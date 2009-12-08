<!-- Remove the next title for production page ...  :-) -->
<H1>A sample script, to show how to manage upload in PHP</H1>
<P>Use it as the postURL parameter, to check upload behavior</P>
<?php
class JUpload {

    var $appletparams;
    var $classparams;
    var $files;

    public function JUpload($appletparams = array(), $classparams = array()) {
        if (gettype($classparams) != 'array')
            $this->abort('Invalid type of parameter classparams: Expecting an array');
        if (gettype($appletparams) != 'array')
            $this->abort('Invalid type of parameter appletparams: Expecting an array');
        // set some defaults for the applet params
        if (!isset($appletparams['name']))
            $appletparams['name'] = 'JUpload';
        if (!isset($appletparams['archive']))
            $appletparams['archive'] = 'wjhk.jupload.jar';
        if (!isset($appletparams['code']))
            $appletparams['code'] = 'wjhk.jupload2.JUploadApplet';
        if (!isset($appletparams['debugLevel']))
            $appletparams['debugLevel'] = 0;
        if (!isset($appletparams['showLogWindow']))
            $appletparams['showLogWindow'] = ($appletparams['debugLevel'] > 0) ? 'true' : 'false';
        if (!isset($appletparams['width']))
            $appletparams['width'] = 640;
        if (!isset($appletparams['height']))
            $appletparams['height'] = ($appletparams['showLogWindow'] == 'true') ? 500 : 300;
        if (!isset($appletparams['mayscript']))
            $appletparams['mayscript'] = 'true';
        if (!isset($appletparams['scriptable']))
            $appletparams['scriptable'] = 'false';
        //if (!isset($appletparams['stringUploadSuccess']))
            $appletparams['stringUploadSuccess'] = 'SUCCESS';
        //if (!isset($appletparams['stringUploadError']))
            $appletparams['stringUploadError'] = 'ERROR: (.*)';
        $maxpost = $this->tobytes(ini_get('post_max_size'));
        $maxmem = $this->tobytes(ini_get('memory_limit'));
        $maxfs = $this->tobytes(ini_get('upload_max_filesize'));
        $obd = ini_get('open_basedir');
        if (!isset($appletparams['maxChunkSize'])) {
            $maxchunk = ($maxpost < $maxmem) ? $maxpost : $maxmem;
            $maxchunk = ($maxchunk < $maxfs) ? $maxchunk : $maxfs;
            $maxchunk /= 4;
            $optchunk = (500000 > $maxchunk) ? $maxchunk : 500000;
            $appletparams['maxChunkSize'] = $optchunk;
        }
        $appletparams['maxChunkSize'] = $this->tobytes($appletparams['maxChunkSize']);
        if (!isset($appletparams['maxFileSize']))
            $appletparams['maxFileSize'] = $maxfs;
        $appletparams['maxFileSize'] = $this->tobytes($appletparams['maxFileSize']);
        if (isset($classparams['errormail'])) {
            $appletparams['urlToSendErrorTo'] = $_SERVER["PHP_SELF"] . '?errormail';
        }
        // Same for class parameters
        if (!isset($classparams['demo_mode']))
            $classparams['demo_mode'] = false;
        if ($classparams['demo_mode']) {
            $classparams['create_destdir'] = false;
            $classparams['allow_subdirs'] = true;
            $classparams['allow_zerosized'] = true;
            $classparams['duplicate'] = 'overwrite';
            $appletparams['afterUploadURL'] = $_SERVER['PHP_SELF'] . '?afterupload=1';
        }
        if (!isset($classparams['create_destdir']))
            $classparams['create_destdir'] = true;
        if (!isset($classparams['allow_subdirs']))
            $classparams['allow_subdirs'] = false;
        if (!isset($classparams['allow_zerosized']))
            $classparams['allow_zerosized'] = false;
        if (!isset($classparams['duplicate']))
            $classparams['duplicate'] = 'rename';
        if (!isset($classparams['dirperm']))
            $classparams['dirperm'] = 0755;
        if (!isset($classparams['fileperm']))
            $classparams['fileperm'] = 0644;
        if (!isset($classparams['destdir'])) {
            if ($obd != '')
                $classparams['destdir'] = $obd;
            else
                $classparams['destdir'] = '/var/tmp/jupload_test';
        }
        if ($classparams['create_destdir'])
            @mkdir($classparams['destdir'], $classparams['dirperm']);
        if (!is_dir($classparams['destdir']) && is_writable($classparams['destdir']))
            $this->abort('Destination dir not accessible');
        if (!isset($classparams['tmp_prefix']))
            $classparams['tmp_prefix'] = 'jutmp.';
        if (!isset($classparams['var_prefix']))
            $classparams['var_prefix'] = 'juvar.';
        if (!isset($classparams['jscript_wrapper']))
            $classparams['jscript_wrapper'] = 'JUploadSetProperty';
        if (!isset($classparams['tag_jscript']))
            $classparams['tag_jscript'] = '<!--JUPLOAD_JSCRIPT-->';
        if (!isset($classparams['tag_applet']))
            $classparams['tag_applet'] = '<!--JUPLOAD_APPLET-->';
        if (!isset($classparams['tag_flist']))
            $classparams['tag_flist'] = '<!--JUPLOAD_FILES-->';
        if (!isset($classparams['http_flist_start']))
            $classparams['http_flist_start'] = "You uploaded the following files:<br />\n<table><tr><td>Filename</td><td>Size</td></tr>\n";
        if (!isset($classparams['http_flist_end']))
            $classparams['http_flist_end'] = "</table>\n";
        if (!isset($classparams['http_flist_file_before']))
            $classparams['http_flist_file_before'] = "<tr><td>";
        if (!isset($classparams['http_flist_file_between']))
            $classparams['http_flist_file_between'] = "</td><td>";
        if (!isset($classparams['http_flist_file_after']))
            $classparams['http_flist_file_after'] = "</td></tr>\n";

        $this->appletparams = $appletparams;
        $this->classparams = $classparams;
        $this->page_start();
    }
    
    /**
     * Log a message on the current output, as a HTML comment.
     */
    private function logDebug($function, $msg, $htmlComment=true) {
    	$output = "[DEBUG] [$function] $msg";
    	if ($htmlComment) {
    		echo("<!-- $output -->\r\n");	
    	} else {
    		echo("$output\r\n");
    	}
    	
    }

    private function tobytes($val) {
        $val = trim($val);
        $last = strtolower($val{strlen($val)-1});
        switch($last) {
        case 'g':
            $val *= 1024;
        case 'm':
            $val *= 1024;
        case 'k':
            $val *= 1024;
        }
        return $val;
    }

    /**
     * Build a string, containing a javascript wrapper function
     * for setting applet properties via JavaScript. This is necessary,
     * because we use the "modern" method of including the applet (using
     * <object> resp. <embed> tags) in order to trigger automatic JRE downloading.
     * Therefore, in Netscape-like browsers, the applet is accessible via
     * the document.embeds[] array while in others, it is accessible via the
     * document.applets[] array.
     *
     * @return A string, containing the necessary wrapper function (named JUploadSetProperty)
     */
    private function str_jsinit() {
        $N = "\n";
        $name = $this->appletparams['name'];
        $ret = '<script type="text/javascript">'.$N;
        $ret .= '<!--'.$N;
        $ret .= 'function '.$this->classparams['jscript_wrapper'].'(name, value) {'.$N;
        $ret .= '  document.applets["'.$name.'"] == null || document.applets["'.$name.'"].setProperty(name,value);'.$N;
        $ret .= '  document.embeds["'.$name.'"] == null || document.embeds["'.$name.'"].setProperty(name,value);'.$N;
        $ret .= '}'.$N;
        $ret .= '//-->'.$N;
        $ret .= '</script>';
        return $ret;
    }

    /**
     * Build a string, containing the applet tag with all parameters.
     *
     * @return A string, containing the applet tag
     */
    private function str_applet() {
        $N = "\n";
        $params = $this->appletparams;
        // return the actual applet tag
        $ret = '<object classid = "clsid:8AD9C840-044E-11D1-B3E9-00805F499D93"'.$N;
        $ret .= '  codebase = "http://java.sun.com/update/1.5.0/jinstall-1_5-windows-i586.cab#Version=5,0,0,3"'.$N;
        $ret .= '  width = "'.$params['width'].'"'.$N;
        $ret .= '  height = "'.$params['height'].'"'.$N;
        $ret .= '  name = "'.$params['name'].'">'.$N;
        foreach ($params as $key => $val) {
            if ($key != 'width' && $key != 'height')
                $ret .= '  <param name = "'.$key.'" value = "'.$val.'" />'.$N;
        }
        $ret .= '  <comment>'.$N;
        $ret .= '    <embed'.$N;
        $ret .= '      type = "application/x-java-applet;version=1.5"'.$N;
        foreach ($params as $key => $val)
            $ret .= '      '.$key.' = "'.$val.'"'.$N;
        $ret .= '      pluginspage = "http://java.sun.com/products/plugin/index.html#download">'.$N;
        $ret .= '      <noembed>'.$N;
        $ret .= '        Java 1.5 or higher plugin required.'.$N;
        $ret .= '      </noembed>'.$N;
        $ret .= '    </embed>'.$N;
        $ret .= '  </comment>'.$N;
        $ret .= '</object>';
        return $ret;
    }

    private function abort($msg = '') {
        // remove all uploaded files of *this* request
        if (isset($_FILES)) {
            foreach ($_FILES as $key => $val)
                @unlink($val['tmp_name']);
        }
        // remove accumulated file, if any.
        @unlink($this->classparams['destdir'].'/'.$this->classparams['tmp_prefix'].session_id());
        @unlink($this->classparams['destdir'].'/'.$this->classparams['tmp_prefix'].'tmp'.session_id());
        // reset session var
        $_SESSION[$this->classparams['var_prefix'].'size'] = 0;
        if ($msg != '')
            die('ERROR: '.$msg."\n");
        exit;
    }

    private function mkdirp($path) {
        // create subdir (hierary) below destdir;
        $dirs = explode('/', $path);
        $path = $this->classparams['destdir'];
        foreach ($dirs as $dir) {
            $path .= '/'.$dir;
            @mkdir($path, $this->classparams['dirperm']);
        }
        if (!is_dir($path) && is_writable($path))
            $this->abort('Destination dir not accessible');
    }

    private function dstfinal($name, $subdir) {
        // replace some potentially dangerous characters
        $name = preg_replace('![`$\\\\/|]!', '_', $name);
        if ($this->classparams['allow_subdirs'] && ($subdir != '')) {
            $subdir = trim(preg_replace('!\\\\!','/',$subdir),'/');
            $subdir = preg_replace('![`$|]!', '_', $subdir);
            // recursively create subdir
            if (!$this->classparams['demo_mode'])
                $this->mkdirp($subdir);
            // append a slash
            $subdir .= '/';
        } else
            $subdir = '';
        $ret = $this->classparams['destdir'].'/'.$subdir.$name;
        if (file_exists($ret)) {
            if ($this->classparams['duplicate'] == 'overwrite')
                return $ret;
            if ($this->classparams['duplicate'] == 'reject')
                $this->abort('A file with the same name already exists');
            if ($this->classparams['duplicate'] == 'rename') {
                $cnt = 1;
                $dir = $this->classparams['destdir'].'/'.$subdir;
                $ext = strrchr($name, '.');
                if ($ext)
                    $name = substr($name, 0, strlen($name) - strlen($ext));
                else
                    $ext = '';
                $rtry = $dir.$name.'.['.$cnt.']'.$ext;
                while (file_exists($rtry)) {
                    $cnt++;
                    $rtry = $dir.$name.'.['.$cnt.']'.$ext;
                }
                return $rtry;
            }
        }
        return $ret;
    }

    // This *must* be public, because it is called from PHP's output buffering
    public function intercept($str) {
    	logDebug('intercept', 'Entering function');
        $flist = '';
        if (count($this->files) > 0) {
        	logDebug('intercept', 'Nb uploaded files is: ' .$this->files);
            $flist = $this->classparams['http_flist_start'];
            $l = strlen($this->classparams['destdir']) + 1;
            foreach ($this->files as $f) {
            	logDebug('intercept', "  Reading file $f");
                $flist .= $this->classparams['http_flist_file_before'];
                $flist .= substr($f, $l);
                $flist .= $this->classparams['http_flist_file_between'];
                if ($this->classparams['demo_mode'])
                    $flist .= 'N/A (demo mode - file not stored)';
                else
                    $flist .= filesize($f);
                $flist .= $this->classparams['http_flist_file_after'];
            }
            $flist .= $this->classparams['http_flist_end'];
        }
        $str = preg_replace('/'.$this->classparams['tag_flist'].'/', $flist, $str);
        $str = preg_replace('/'.$this->classparams['tag_jscript'].'/', $this->str_jsinit(), $str);
        return preg_replace('/'.$this->classparams['tag_applet'].'/', $this->str_applet(), $str);
    }

    private function page_start() {
    	logDebug('page_start', 'Entering function');
    	
        // If the applet checks for the serverProtocol, it issues a HEAD request
        // -> Simply return an empty doc.
        if ($_SERVER['REQUEST_METHOD'] == 'HEAD')
            exit;

        // A GET request means: return upload page
        if ($_SERVER['REQUEST_METHOD'] == 'GET') {
        	logDebug('page_start', 'Entering GET management');
            session_start();
            if (isset($_GET['afterupload'])) {
                if (!isset($_SESSION[$this->classparams['var_prefix'].'files']))
                    $this->abort('Invalid session (in afterupload, GET, check of $_SESSION)');
                $this->files = $_SESSION[$this->classparams['var_prefix'].'files'];
                if (!is_array($this->files))
                    $this->abort('Invalid session (in afterupload, GET, check of is_array(files))');
            } else {
                $this->files = array();
                $_SESSION[$this->classparams['var_prefix'].'size'] = 0;
                $_SESSION[$this->classparams['var_prefix'].'files'] = $this->files;
            }
            // start intercepting the content of the calling page
            ob_start(array(& $this, 'intercept'));
            return;
        }

        // If we got a POST request, this is the real work.
        if ($_SERVER['REQUEST_METHOD'] == 'POST') {
        	logDebug('page_start', 'Entering POST management');
            session_start();
            if (isset($_GET['errormail'])) {
                // handle error report
                if (isset($_POST['description']) && isset($_POST['log'])) {
                    $msg = $_POST['log'];
                    mail($this->classparams['errormail'], $_POST['description'], $msg);
                } else {
                    if (isset($_SERVER['SERVER_ADMIN']))
                        mail($_SERVER['SERVER_ADMIN'], 'Empty jupload error log',
                            'An empty log has just been posted.');
                    error_log('Empty error log received', 0);
                }
                exit;
            }
            // we check for the session *after* handling possible error log
            // because an error could have happened because the session-id is missing.
            if (!isset($_SESSION[$this->classparams['var_prefix'].'size']))
                $this->abort('Invalid session (in afterupload, POST, check of size)');
            if (!isset($_SESSION[$this->classparams['var_prefix'].'files']))
                $this->abort('Invalid session (in afterupload, POST, check of files)');
            $this->files = $_SESSION[$this->classparams['var_prefix'].'files'];
            if (!is_array($this->files))
                $this->abort('Invalid session (in afterupload, POST, is_array(files))');
            if (!isset($_POST['md5sum']))
                $this->abort('Required POST variable md5sum is missing');
            $cnt = 0;
            foreach ($_FILES as $key => $value) {
                $jupart = (isset($_POST['jupart'])) ? (int)$_POST['jupart'] : 0;
                $jufinal = (isset($_POST['jufinal'])) ? (int)$_POST['jufinal'] : 1;
                $relpaths = (isset($_POST['relpathinfo'])) ? $_POST['relpathinfo'] : null;
                $md5sums = (isset($_POST['md5sum'])) ? $_POST['md5sum'] : null;
                //$relpaths = (isset($_POST["relpathinfo$cnt"])) ? $_POST["relpathinfo$cnt"] : null;
                //$md5sums = (isset($_POST["md5sum$cnt"])) ? $_POST["md5sum$cnt"] : null;
                if (gettype($relpaths) == 'string')
                    $relpaths = array($relpaths);
                if (gettype($md5sums) == 'string')
                    $md5sums = array($md5sums);
                if (!is_array($md5sums))
                    $this->abort('Expecting an array of MD5 checksums');
                if (!is_array($relpaths))
                    $this->abort('Expecting an array of relative paths');
                $dstdir = $this->classparams['destdir'];
                $dstname = $dstdir.'/'.$this->classparams['tmp_prefix'].session_id();
                $tmpname = $dstdir.'/'.$this->classparams['tmp_prefix'].'tmp'.session_id();
                if (!move_uploaded_file($value['tmp_name'], $tmpname))
                    $this->abort("Unable to move uploaded file (from ${value['tmp_name']} to $tmpname)");
                if ($this->classparams['demo_mode']) {
                    if ($jufinal || (!$jupart))
                        array_push($this->files, $this->dstfinal($value['name'],$relpaths[$cnt]));
                    unlink($tmpname);
                    $cnt++;
                    continue;
                }
                if ($jupart) {
                    // got a chunk of a multi-part upload
                    $len = filesize($tmpname);
                    $_SESSION[$this->classparams['var_prefix'].'size'] += $len;
                    if ($len > 0) {
                        $src = fopen($tmpname, 'rb');
                        $dst = fopen($dstname, ($jupart == 1) ? 'wb' : 'ab');
                        while ($len > 0) {
                            $rlen = ($len > 8192) ? 8192 : $len;
                            $buf = fread($src, $rlen);
                            if (!$buf) {
                                fclose($src);
                                fclose($dst);
                                unlink($dstname);
                                $this->abort('read IO error');
                            }
                            if (!fwrite($dst, $buf, $rlen)) {
                                fclose($src);
                                fclose($dst);
                                unlink($dstname);
                                $this->abort('write IO error');
                            }
                            $len -= $rlen;
                        }
                        fclose($src);
                        fclose($dst);
                        unlink($tmpname);
                    }
                    if ($jufinal) {
                        // This is the last chunk. Check total lenght and
                        // rename it to it's final name.
                        $dlen = filesize($dstname);
                        if ($dlen != $_SESSION[$this->classparams['var_prefix'].'size'])
                            $this->abort('file size mismatch');
                        if ($md5sums[$cnt] != md5_file($dstname))
                      	    $this->abort('MD5 checksum mismatch');
                        // remove zero sized files
                        if (($dlen > 0) || $this->classparams['allow_zerosized']) {
                            $dstfinal = $this->dstfinal($value['name'],$relpaths[$cnt]);
                            if (!rename($dstname, $dstfinal))
                                $this->abort('rename IO error');
                            if (!chmod($dstfinal, $this->classparams['fileperm']))
                                $this->abort('chmod IO error');
                            array_push($this->files, $dstfinal);
                        } else
                            unlink($dstname);
                        // reset session var
                        $_SESSION[$this->classparams['var_prefix'].'size'] = 0;
                    }
                } else {
                    // Got a single file upload. Trivial.
                    if ($md5sums[$cnt] != md5_file($tmpname))
                        $this->abort('MD5 checksum mismatch');
                    $dstfinal = $this->dstfinal($value['name'],$relpaths[$cnt]);
                    if (!rename($tmpname, $dstfinal))
                        $this->abort('rename IO error');
                    if (!chmod($dstfinal, $this->classparams['fileperm']))
                        $this->abort('chmod IO error');
                    array_push($this->files, $dstfinal);
                }
                $cnt++;
            }
            echo "SUCCESS\n";
            $_SESSION[$this->classparams['var_prefix'].'files'] = $this->files;
            session_write_close();
            exit;
        }
    }
}



// Let's use this class now !
$JUpload = new JUpload();

// PHP end tag omitted intentionally!!
