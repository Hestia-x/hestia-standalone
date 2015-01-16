package huck.hestia.db;


public interface CreditCode {
	public int id();
	public String name();
	public Asset asset();
	public String defaultDescription();
}
