<?php
include ('conn.php');



$db_name = "family";
$username = "root";
$password = "";
$servename = "127.0.0.1";
$conn= mysqli_connect($servename ,$username ,$password ,$db_name );
mysqli_set_charset($conn ,"utf-8");

	if($_SERVER['REQUEST_METHOD'] == 'POST')
	{
$fname = $_POST["fname"];
$sname = $_POST["sname"];
$gender = $_POST["gender"];
$email = $_POST["email"];
$password = $_POST["psw"];
$mobile = $_POST["mobile"];
$dob = $_POST["dob"];

//$username = "Abdo"; $email = "abdelhamid@yahoo.com"; $password = "123456"; $mobile = "01222225522"; $gender = "Male";/
//$isValidEMail = filter_var($email , FILTER_VALIDATE_EMAIL);
// if($conn){
// if(strlen($password ) > 40 || strlen($password ) < 6){
// echo "Password length must be more than 6 and less than 40";
// }
// else if($isValidEMail === false){
// echo "This Email is not valid";
// }
// else if(strlen($mobile ) > 10 || strlen($mobile ) < 10)){
// echo "This phone number is not valid";
// }else{
// $sqlCheckUname = "SELECT * FROM users WHERE phoneno LIKE '$mobile'";
// $u_name_query = mysqli_query($conn, $sqlCheckUname);
// $sqlCheckEmail = "SELECT * FROM users WHERE email LIKE '$email'";
// $email_query = mysqli_query($conn, $sqlCheckEmail);
// if(mysqli_num_rows($u_name_query) > 0){
// echo "User name allready used type another one";
// }else if(mysqli_num_rows($email_query) > 0){
// echo "This Email is allready registered";
// }else{
$sql_register = "INSERT INTO users (fname,sname,gender,dob,email,phoneno,passwd) VALUES ('$fname','$sname','$gender','$dob','$email','$mobile','$password')";
$yea=mysqli_query($conn,$sql_register);

if($yea){
echo "You are registered successfully";
}else{
echo "Failed to register you account";
}
}
// else{
// echo "Connection Error";
// }
// echo "Connection Error";
?>
