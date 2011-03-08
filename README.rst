Sfalma Android
==========================

How to use it
------

After you build the project (see Building section), copy sfalma-trace.jar into your project libraries and import it to your project:
	
	import com.sfalma.trace.SfalmaHandler;

then just add the SfalmaHandler after setContentView and you are ready to go.


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        SfalmaHandler.setup(this);

        buildUserInterface();
    }


This is straightforward: The setup() call install Sfalma exception handler 
and submit any existing traces from earlier crashes.

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

Copy "local.properties.template" to "local.properties", and edit it to
set the correct "lib.dir" path to your Android SDK platform. Then run:

    $ ant package

