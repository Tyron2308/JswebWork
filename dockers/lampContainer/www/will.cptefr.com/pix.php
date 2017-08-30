<?php
header("Content-Type: image/png");

define("SPACE_TEMPORARY_WORKING","/jsweb/");

function ip_visiteur($idf_local = 0) {
	 if ($idf_local != 0) {
	     return $idf_local;
	 }
	 $ip = "";
	 if (!empty($_SERVER['HTTP_X_FORWARDED_FOR'])) {
	     // if (@geoip_record_by_name($_SERVER['HTTP_X_FORWARDED_FOR']) !== FALSE) {
	     //     $ip = $_SERVER['HTTP_X_FORWARDED_FOR'];
	     // } elseif (@geoip_record_by_name($_SERVER['REMOTE_ADDR']) !== FALSE) {
	     //     $ip = $_SERVER['REMOTE_ADDR'];
	     // } else {
		 $ip = $_SERVER['HTTP_X_FORWARDED_FOR'];
	     // }
	 } else {
	     $ip = @$_SERVER['REMOTE_ADDR'];
	 }
	 if ($ip == "127.0.0.1") {
	     $ip = $_SERVER['REMOTE_ADDR'];
	 }

	 $test_ip = explode(',', $ip);

	 if (is_array($test_ip) && !empty($test_ip) && count($test_ip) > 0) {
	     if (in_array("127.0.0.1", $test_ip)) {
		 $ip = $_SERVER['REMOTE_ADDR'];
	     } elseif (isset($test_ip[0])) {
		 $ip = $test_ip[0];
	     }
	 } elseif (isset($test_ip[0])) {
	     $ip = $test_ip[0];
	 }

	 return $ip;
}

function user_agent() {
	 $user_agent = !empty($_SERVER['HTTP_USER_AGENT']) ? $_SERVER['HTTP_USER_AGENT'] : "";
	 return $user_agent;
}

function add_file($type, $boutique, $ref, $category_id,$product_id,$transaction_amount,$transaction_id){
	$chemin = SPACE_TEMPORARY_WORKING.'access_'.date("Y_m_d_H_i").".log";
	$fp = fopen($chemin, 'a+');
	fwrite($fp, ip_visiteur()." - - [".date("d/m/Y:H:i:s")." +0200] \"GET /trafic/retar.php?type=".$type."&boutique=".$boutique."&category_id=".$category_id."&produit_id=".$product_id."&transaction_amount=".$transaction_amount."&transaction_id=".$transaction_id."&refer=".$ref." HTTP/1.1\" 200 821 \"".$ref."\" \"".user_agent()."\" PID 43391 TimeTaken 1910\n");
	fclose($fp);
}

function save_file($type, $boutique, $ref, $category_id,$product_id,$transaction_amount,$transaction_id){
	$fp = fsockopen("tcp://149.202.91.15", 5005, $errno, $errstr, 30);
	if (!$fp) {
		error_log("$errstr ($errno)", 0);
		echo "$errstr ($errno)<br />\n";
	} else {
		fwrite($fp, ip_visiteur()." - - [".date("d/m/Y:H:i:s")." +0200] \"GET /trafic/retar.php?type=".$type."&boutique=".$boutique."&category_id=".$category_id."&produit_id=".$product_id."&transaction_amount=".$transaction_amount."&transaction_id=".$transaction_id."&refer=".$ref." HTTP/1.1\" 200 821 \"".$ref."\" \"".user_agent()."\" PID 43391 TimeTaken 1910\n");
		fclose($fp);
	}
}

$type = !empty($_GET['type']) ? $_GET['type'] : '';
$boutique = !empty($_GET['boutique']) ? $_GET['boutique'] : '';
$ref = !empty($_GET['refer']) ? $_GET['refer'] : '';
$category_id = !empty($_GET['category_id']) ? $_GET['category_id'] : '';
$product_id = !empty($_GET['produit_id']) ? $_GET['produit_id'] : '';
$transaction_id = !empty($_GET['transaction_id']) ? $_GET['transaction_id'] : '';
$transaction_amount = !empty($_GET['transaction_amount']) ? $_GET['transaction_amount'] : '';
add_file($type, $boutique, $ref, $category_id,$product_id,$transaction_amount,$transaction_id);
save_file($type, $boutique, $ref, $category_id,$product_id,$transaction_amount,$transaction_id);




$opts = array(
		'http'	=> array(
		'method'			=> 'GET',
		'protocol_version'	=> '1.1',
		'timeout'			=> 5,
		'user_agent'		=> 'Veoxa 2.0'
));
// Initialize stream context
$context = stream_context_create($opts);
ob_start();
$content = @file_get_contents('images/pixel.png', false, $context);
ob_end_clean();
print $content;
?>
