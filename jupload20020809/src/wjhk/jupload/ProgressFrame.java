package wjhk.jupload;

public interface ProgressFrame {
  public void show();
  public void updateDisplay(FileUploadThread fut);
}