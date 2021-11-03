package scc.srv;

import scc.srv.resources.MessageResource;
import scc.srv.resources.UserResource;
import scc.srv.resources.MediaResource;

import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

public class MainApplication extends Application {
    private final Set<Object> singletons = new HashSet<Object>();
    private final Set<Class<?>> resources = new HashSet<Class<?>>();

    public MainApplication() {
        resources.add(ControlResource.class);
        //singletons.add(new UserResource());
        singletons.add(new MessageResource());
        singletons.add(new MediaResource());
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
