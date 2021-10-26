<?php

	include ('conn.php');

	if($_SERVER['REQUEST_METHOD'] == 'POST')
	{
		// posting variables to..
		$email=$_POST['fname'];
		$password=$_POST['sname'];
		$que=mysqli_query($conn,"select * from users where phoneno='$email' and passwd='$password'");
	//	$row=mysqli_num_rows($que);

		if($que)
		{
			echo "Login succesful";
	        }

		else
			{
				echo "Login Unsuccessful";
			}
	}



	?>
