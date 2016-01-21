package unipd.astro;

public interface AsyncCallback {
	void OnResponseReceived(String response);
	void OnErrorReceived(String error);
}
