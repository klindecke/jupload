/**
 * This applet is just an empty applet ...
 * 
 * I use it to use the HtmlConverter binary 'given' in the JDK:
 * <DIR>
 * <LI> I embed an instance of the EmptyApplet in my page, that contains the JUploadApplet: the user will be prompt
 * to download the JRE of the correct version, if it nos installed.
 * <LI> The JUploadApplet is created through the basic APPLET tag: it's much simplier this way, as I often change
 * the applet parameters.
 * </DIR>
 *  <BR>
 * 
 *  Below is a sample of the HTML code you can insert in you HTML/ASP/PHP/JSP/ or whatever page. It has been 
 *  generated by using the jdk tool HtmlConverter from a simple applet tag (see the applet commented, at the
 *  end of the sample).
 *  <BR>
 *  This sample has been generate from an applet tag. Just replace all occurences of
 *  <I>plugins/jupload/wjhk.jupload.jar</I> by the URL of your jar file that contains the empty applet.
 *   

<!--"CONVERTED_APPLET"-->
<!-- The next applet is an empty one. It uses for one thing: prompt the --> 
<!-- user to download the JRE, if it's not installed on its computer'   -->
<!-- HTML CONVERTER -->
<script language="JavaScript" type="text/javascript"><!--
    var _info = navigator.userAgent;
    var _ns = false;
    var _ns6 = false;
    var _ie = (_info.indexOf("MSIE") > 0 && _info.indexOf("Win") > 0 && _info.indexOf("Windows 3.1") < 0);
//--></script>
    <comment>
        <script language="JavaScript" type="text/javascript"><!--
        var _ns = (navigator.appName.indexOf("Netscape") >= 0 && ((_info.indexOf("Win") > 0 && _info.indexOf("Win16") < 0 && java.lang.System.getProperty("os.version").indexOf("3.5") < 0) || (_info.indexOf("Sun") > 0) || (_info.indexOf("Linux") > 0) || (_info.indexOf("AIX") > 0) || (_info.indexOf("OS/2") > 0) || (_info.indexOf("IRIX") > 0)));
        var _ns6 = ((_ns == true) && (_info.indexOf("Mozilla/5") >= 0));
//--></script>
    </comment>

<script language="JavaScript" type="text/javascript"><!--
    if (_ie == true) document.writeln('<object classid="clsid:8AD9C840-044E-11D1-B3E9-00805F499D93" WIDTH = "0" HEIGHT = "0" NAME = "EmptyApplet"  codebase="http://java.sun.com/update/1.5.0/jinstall-1_5-windows-i586.cab#Version=5,0,0,3"><noembed><xmp>');
    else if (_ns == true && _ns6 == false) document.writeln('<embed ' +
	    'type="application/x-java-applet;version=1.5" \
            CODE = "wjhk.jupload2.EmptyApplet" \
            ARCHIVE = "plugins/jupload/wjhk.jupload.jar" \
            NAME = "EmptyApplet" \
            WIDTH = "0" \
            HEIGHT = "0" ' +
	    'scriptable=false ' +
	    'pluginspage="http://java.sun.com/products/plugin/index.html#download"><noembed><xmp>');
//--></script>
<applet  CODE = "wjhk.jupload2.EmptyApplet" ARCHIVE = "plugins/jupload/wjhk.jupload.jar" WIDTH = "0" HEIGHT = "0" NAME = "EmptyApplet"></xmp>
    <PARAM NAME = CODE VALUE = "wjhk.jupload2.EmptyApplet" >
    <PARAM NAME = ARCHIVE VALUE = "plugins/jupload/wjhk.jupload.jar" >
    <PARAM NAME = NAME VALUE = "EmptyApplet" >
    <param name="type" value="application/x-java-applet;version=1.5">
    <param name="scriptable" value="false">

Java 1.4 or higher plugin required.
</applet>
</noembed>
</embed>
</object>

<!--
<APPLET CODE = "wjhk.jupload2.EmptyApplet" ARCHIVE = "plugins/jupload/wjhk.jupload.jar" WIDTH = "0" HEIGHT = "0" NAME = "EmptyApplet">
Java 1.4 or higher plugin required.

</APPLET>
-->
<!--"END_CONVERTED_APPLET"-->


 */
package wjhk.jupload2;

import java.applet.Applet;

public class EmptyApplet extends Applet {
	
	private static final long serialVersionUID = 1L;

	public EmptyApplet() {
		//We do ...  an empty constructor ...   ;-)
	}

}
