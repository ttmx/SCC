package scc.srv;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

public class MainApplication extends Application
{
	private Set<Object> singletons = new HashSet<Object>();
	private Set<Class<?>> resources = new HashSet<Class<?>>();

	public MainApplication() {
		resources.add(ControlResource.class);
//		resources.add(MediaResource.class);
		singletons.add( new MediaResource());	
	}

	@Override
	public Set<Class<?>> getClasses() {
		return resources;
	}

	@Override
	public Set<Object> getSingletons() {
		return singletons;
	}
}
