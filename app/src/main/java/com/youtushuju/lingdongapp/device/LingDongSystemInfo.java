package com.youtushuju.lingdongapp.device;

public final class LingDongSystemInfo
{
    public String api_version;
    public String device_model;
    public String android_os_version;
    
    public String running_memory;
    public String internal_storage_memory;
    public String internal_free_storage_memory;
    
    public String kernel_version;
    public String builder_number_display;
    
    public String toString()
    {
        return String.format("Api version(%s), Device model(%s), Android OS version(%s), Running memory(%s), Internal memory(%s/%s), Kernel version(%s), Builder number display(%s)"
        , api_version, device_model, android_os_version, running_memory, internal_free_storage_memory, internal_storage_memory, kernel_version, builder_number_display);
    }
}
