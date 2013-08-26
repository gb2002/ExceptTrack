ExceptTrack
==========================
This is a fork of the BugSense-Android library.

How to use it
------

You build the project (see Building section), copy ExceptTrack.jar into your project libraries and import it to your project or you could copy ExceptTrack.jar :
	
	import com.twww.excepttrack.ExceptTrackHandler;




then just add the ExceptTrackHandler after setContentView and you are ready to go.


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
	ExceptTrack.setUrl(yourcollectionURL);

	ExceptTrack.setup(this, uniqueID);


        buildUserInterface();
    }

Note: uniqueID is any string. It is output with the response.  One suggested use is during testing you can insert a clientid to
tie to a specific phone.  However this usage is not recommended during production due to privacy concerns. 

yourcollectionURL is the full web address to your collection script.  Be sure to include a trailing / if it is a directory 
as it will not redirect.

This is straightforward: Just call setUrl() to setup your collection URL.  Then the setup() call installs ExceptTrack exception handler 
and submits any existing traces from earlier crashes.

Custom processor
----------------

If you would like to customize the process, for example letting the user
know about the stack trace submission, you can use a processor:

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        ExceptionHandler.setup(this, new ExceptionHandler.Processor() {
            @Override
            public boolean beginSubmit() {
                mExceptionSubmitDialog = AlertDialog.Builder().create();
                return true;
            }

            @Override
            public void submitDone() {
                mExceptionSubmitDialog.cancel();
            }

            @Override
            public void handlerInstalled() {}
        }));

        buildUserInterface();
    }


You probably want to make the dialog have no buttons and set "cancelable"
to false.


Asking the user
---------------

You may want to ask the user if he agrees with submitting the trace.
This is really easy as well, if somewhat awkward to write:

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        ExceptionHandler.setup(this, new ExceptionHandler.Processor() {
            @Override
            public boolean beginSubmit() {
                // Don't submit traces that may exist, we just
                // install the handler.
                return false;
            }
            @Override
            public void submitDone() {}
            @Override
            public void handlerInstalled() {}
        }));

        // Manually have a look at whether there are traces, and if so,
        // ask the user if we may submit them.
        if (ExceptionHandler.hasStrackTraces())
            askUserIfWeMaySubmit();
    }

    private void askUserPermissionResult(boolean permissionGranted) {
        if (!permissionGranted) {
            // Clear the traces we won't submit now from memory.
            ExceptionHandler.clear();
        }
        else {
            ExceptionHandler.submit();
        }
    }





setMinDelay() allows you to specify a minimum time that needs to pass
before the submitDone() callback is executed. Useful if you don't want
UI elements that you have specifically shown to indicate trace submission
to flicker-like disappear again.

setHttpTimeout() to change the default timeout for the HTTP submission.


Building
========

Note: This project is now partially Maven enabled.  The POM file should get you started.  It is
set for Snapshot mode for now but will be changed to release in the future.

Copy "local.properties.template" to "local.properties", and edit it to
set the correct "lib.dir" path to your Android SDK platform. Then run:

    $ ant package

