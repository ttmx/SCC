package scc.srv;

import scc.srv.resources.ChannelResource;
import scc.srv.resources.MediaResource;
import scc.srv.resources.MessageResource;
import scc.srv.resources.UserResource;

import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

public class MainApplication extends Application {
    private final Set<Object> singletons = new HashSet<Object>();
    private final Set<Class<?>> resources = new HashSet<Class<?>>();

    public MainApplication() {
        DataAbstractionLayer data = new DataAbstractionLayer();
        resources.add(ControlResource.class);
        singletons.add(new UserResource(data));
        singletons.add(new MessageResource(data));
        singletons.add(new ChannelResource(data));
        singletons.add(new MediaResource(data));
        singletons.add(data);
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
