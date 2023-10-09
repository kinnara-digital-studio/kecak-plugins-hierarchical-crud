package com.kinnarastudio.kecakplugins.hcrudmenu;

import com.kinnarastudio.kecakplugins.hcrudmenu.menu.HierarchicalCrudMenu;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import java.util.ArrayList;
import java.util.Collection;

public class Activator implements BundleActivator {

    protected Collection<ServiceRegistration> registrationList;

    public void start(BundleContext context) {
        registrationList = new ArrayList<ServiceRegistration>();

        //Register plugin here
        registrationList.add(context.registerService(HierarchicalCrudMenu.class.getName(), new HierarchicalCrudMenu(), null));
//        registrationList.add(context.registerService(HierarchicalCrudFormBinder.class.getName(), new HierarchicalCrudFormBinder(), null));
    }

    public void stop(BundleContext context) {
        for (ServiceRegistration registration : registrationList) {
            registration.unregister();
        }
    }
}