<?php
foreach ($_FILES as $val) {
    unlink($val['tmp_file']);
}
echo "SUCCESS\n";
