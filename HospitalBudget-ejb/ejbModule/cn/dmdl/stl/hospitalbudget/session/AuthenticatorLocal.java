package cn.dmdl.stl.hospitalbudget.session;

public interface AuthenticatorLocal {

	public boolean authenticate();

	public String login();

	public void logout();

	public void remove();

	public void destroy();

}
