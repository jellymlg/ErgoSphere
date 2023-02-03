package fanta.ergosphere.main;

import fanta.ergosphere.util.General;
import java.io.File;
import oshi.PlatformEnum;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OSFileStore;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;

public final class Info {

    private static final SystemInfo INFO = new SystemInfo();

    private static final PlatformEnum PLATFORM = SystemInfo.getCurrentPlatform();

    private static final OperatingSystem OS = INFO.getOperatingSystem();

    private static final HardwareAbstractionLayer HARDWARE = INFO.getHardware();
    
    private static final OSFileStore DISK = getDisk();

    private static final HardwareAbstractionLayer HAL = INFO.getHardware();

    private static final CentralProcessor CPU = HAL.getProcessor();

    private static long[] prevTicks = CPU.getSystemCpuLoadTicks();

    public static double getCPULoad() {
        double load = CPU.getSystemCpuLoadBetweenTicks(prevTicks) * 100;
        prevTicks = CPU.getSystemCpuLoadTicks();
        return load;
    }

    public static float getFullSpace_GB() {
        return (float) (DISK.getTotalSpace() / 1024L / 1024L / 1024D);
    }

    public static float getAvailSpace_GB() {
        return (float) (DISK.getFreeSpace() / 1024L / 1024L / 1024D);
    }

    public static float getUsedSpace_GB() {
        return (float) ((DISK.getTotalSpace() - DISK.getFreeSpace()) / 1024L / 1024L / 1024D);
    }

    public static int getFullMem_MB() {
        return General.byteToMB(HARDWARE.getMemory().getTotal());
    }

    public static int getAvailMem_MB() {
        return General.byteToMB(HARDWARE.getMemory().getAvailable());
    }

    public static int getUsedMem_MB() {
        return getFullMem_MB() - getAvailMem_MB();
    }

    public static int getProcessCount() {
        return OS.getProcessCount();
    }

    public static boolean isWindows() {
        return PLATFORM.equals(PlatformEnum.WINDOWS);
    }

    public static boolean isUnknown() {
        return !isWindows() && !PLATFORM.equals(PlatformEnum.LINUX) && !PLATFORM.equals(PlatformEnum.MACOS);
    }

    public static boolean isAdmin() {
        return OS.isElevated();
    }

    public static OSProcess getProcess(long pid) {
        return OS.getProcess((int) pid);
    }

    private static OSFileStore getDisk() {
        final String path = new File(Manager.getStorage()).getAbsolutePath();
        for(OSFileStore osf : OS.getFileSystem().getFileStores()) if(path.startsWith(osf.getMount())) return osf;
        return OS.getFileSystem().getFileStores().get(0);
    }
}
