package wjhk.jupload;

public class AfterUploadSuccImp implements AfterUploadSucc{
  public void executeThis(StringBuffer svrReturn, StatusPanel sp){
    // You should do your own implementation for processing of files.
    sp.writeStatus("Server Output :> " + svrReturn.toString() + "\n");
  }
}