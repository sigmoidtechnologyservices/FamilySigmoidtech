<?php
$conn=mysqli_connect("localhost","root","",'family');
$phone=$_POST["phone"];
$stmt = $conn->prepare("SELECT  `fname`, `sname` FROM users where phoneno=$phone");
$stmt -> execute();
$stmt -> bind_result($fname,$sname);
$objects=array();

while($stmt ->fetch()){

    $temp=array();
        // $temp['id']=$id;
    $temp['fname']=$fname;
    $temp['sname']=$sname;
    // $temp['gender']=$gender;
    // $temp['dob']=$dob;
    // $temp['email']=$email;
    // $temp['phone']=$phone;
    //   $temp['pass']=$pass;

    array_push($objects,$temp);
}
echo json_encode($objects);




?>
