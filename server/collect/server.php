<?php
        if ( $_POST['data'] == "" ) {
        	// Uncomment and change the following line to have exceptions mailed to you
        mail("george.baker@rocketfarmstudios.com","IMPORTANT: Exception received (".$version.")",$_POST['data'], "from:george.baker@rocketfarmstudios.com");
        
         }
      
	?>
