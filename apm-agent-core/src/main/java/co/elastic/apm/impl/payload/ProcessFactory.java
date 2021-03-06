package co.elastic.apm.impl.payload;

import org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A {@link ProcessFactory} is responsible for creating the {@link Process} object, containing information about the current process.
 */
public interface ProcessFactory {

    /**
     * @return the {@link Process} information about the current process
     */
    Process getProcessInformation();

    /**
     * Redirects to the the best {@link ProcessFactory} strategy for the current VM
     */
    enum ForCurrentVM implements ProcessFactory {

        /**
         * The singleton instance
         */
        INSTANCE;

        /**
         * The best {@link ProcessFactory} for the current VM
         */
        private final ProcessFactory dispatcher;

        ForCurrentVM() {
            dispatcher = ForJava9CompatibleVM.make();
        }

        @Override
        public Process getProcessInformation() {
            return dispatcher.getProcessInformation();
        }
    }

    /**
     * A {@link ProcessFactory} for a legacy VM that reads process information from its JMX properties.
     */
    enum ForLegacyVM implements ProcessFactory {

        /**
         * The singleton instance
         */
        INSTANCE;

        private final RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();

        public Process getProcessInformation() {
            Process process = new Process();
            process.withPid(getPid());
            process.withArgv(runtimeMXBean.getInputArguments());
            process.withTitle(getTitle());
            return process;
        }

        private String getTitle() {
            String javaHome = java.lang.System.getProperty("java.home");
            final String title = javaHome + File.separator + "bin" + File.separator + "java";
            if (java.lang.System.getProperty("os.name").startsWith("Win")) {
                return title + ".exe";
            }
            return title;
        }

        private int getPid() {
            // format: pid@host
            String pidAtHost = runtimeMXBean.getName();
            Matcher matcher = Pattern.compile("(\\d+)@.*").matcher(pidAtHost);
            if (matcher.matches()) {
                return Integer.parseInt(matcher.group(1));
            } else {
                return 0;
            }

        }

    }

    /**
     * A {@link ProcessFactory} for a Java 9+ VMs that reads process information from the {@link ProcessHandle#current() current}
     * {@link ProcessHandle}.
     */
    @IgnoreJRERequirement
    class ForJava9CompatibleVM implements ProcessFactory {

        /**
         * The {@link ProcessHandle} instance, obtained by {@link ProcessHandle#current()}
         * <p>
         * This is stored in a {@link Object} reference as opposed to a {@link ProcessHandle} reference so that reflectively
         * inspecting the instance variables of this class, does not lead to {@link ClassNotFoundException}s on non Java 9 capable VMs
         * </p>
         */
        private Object processHandle;

        /**
         * @param current the {@link ProcessHandle#current} method
         */
        ForJava9CompatibleVM(Method current) {
            processHandle = null;
            try {
                processHandle = current.invoke(null);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Can't access ProcessHandle#current", e);
            } catch (InvocationTargetException e) {
                throw new IllegalStateException("Can't invoke ProcessHandle#current", e);
            }
        }

        /**
         * @return a {@link ProcessFactory} which depends on APIs intruduced in Java 9. Returns a fallback if not running on
         * Java 9.
         */
        public static ProcessFactory make() {
            try {
                return new ForJava9CompatibleVM(ProcessHandle.class.getMethod("current"));
            } catch (Exception ignore) {
                return ForLegacyVM.INSTANCE;
            }
        }

        /**
         * Ideally, this would only reflectively invoke the Java 9 introduced process API.
         * But this is quite a hassle,
         * especially when handling the {@link java.util.Optional} return types of the process API.
         * As we are directly referring to APIs introduced in Java 9,
         * this project can only be compiled with a JDK 9+.
         */
        public Process getProcessInformation() {
            final Process process = new Process();
            ProcessHandle processHandle = (ProcessHandle) this.processHandle;
            process.withPid(processHandle.pid());
            process.withPpid(processHandle.parent()
                .map(new Function<ProcessHandle, Long>() {
                    @Override
                    public Long apply(ProcessHandle processHandle1) {
                        return processHandle1.pid();
                    }
                })
                .orElse(null));
            process.withArgv(processHandle.info()
                .arguments()
                .map(new Function<String[], List<String>>() {
                    @Override
                    public List<String> apply(String[] a) {
                        return Arrays.asList(a);
                    }
                })
                .orElse(null));
            process.withTitle(processHandle.info().command().orElse(null));

            return process;

        }
    }

}
