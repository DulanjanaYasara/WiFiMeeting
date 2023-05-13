package com.example.wifimeeting.usecase.smallgroupdiscussion;

import org.fourthline.cling.binding.annotations.AnnotationLocalServiceBinder;
import org.fourthline.cling.binding.annotations.UpnpAction;
import org.fourthline.cling.binding.annotations.UpnpInputArgument;
import org.fourthline.cling.binding.annotations.UpnpOutputArgument;
import org.fourthline.cling.binding.annotations.UpnpService;
import org.fourthline.cling.binding.annotations.UpnpServiceId;
import org.fourthline.cling.binding.annotations.UpnpServiceType;
import org.fourthline.cling.binding.annotations.UpnpStateVariable;
import org.fourthline.cling.model.meta.LocalService;

import java.beans.PropertyChangeSupport;

@UpnpService(
        serviceId = @UpnpServiceId("GroupMember"),
        serviceType = @UpnpServiceType(value = "GroupMember", version = 1)
)
public class GroupMemberService {
    private final PropertyChangeSupport propertyChangeSupport;

    public GroupMemberService() {
        this.propertyChangeSupport = new PropertyChangeSupport(this);
    }

    public PropertyChangeSupport getPropertyChangeSupport() {
        return propertyChangeSupport;
    }

    @UpnpStateVariable(defaultValue = "1", name = "isMute")
    private boolean isMute = true;

    @UpnpAction
    public void setMute(@UpnpInputArgument(name = "NewIsMuteValue", stateVariable = "isMute") boolean newIsMuteValue) {
        boolean oldIsMuteValue = isMute;
        isMute = newIsMuteValue;

        getPropertyChangeSupport().firePropertyChange("isMute", oldIsMuteValue, isMute);

    }

    @UpnpAction(out = @UpnpOutputArgument(name = "RetIsMuteValue", stateVariable = "isMute"))
    public boolean getMute() {
        return isMute;
    }

    public LocalService getLocalService() {
        AnnotationLocalServiceBinder binder = new AnnotationLocalServiceBinder();
        return binder.read(this.getClass());
    }

}
