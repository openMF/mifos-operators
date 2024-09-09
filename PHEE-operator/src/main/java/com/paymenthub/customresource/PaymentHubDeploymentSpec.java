package com.paymenthub.customresource;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import java.util.*; 

public class PaymentHubDeploymentSpec {
    private Boolean enabled;
    private Map<String, String> labels;
    private VolMount volMount;   
    private Integer replicas;
    private String image;
    private Integer containerPort;
     private Resources resources;
     private Probe livenessProbe;
    private Probe readinessProbe;
    private Boolean rbacEnabled;
    private Boolean secretEnabled;
    private Boolean configMapEnabled;
    private Boolean ingressEnabled;
    private Ingress ingress;
    private List<Service> services;
    private List<EnvironmentVariable> environment;
    private Boolean initContainerEnabled;

    public PaymentHubDeploymentSpec() {
    }

    public PaymentHubDeploymentSpec(Boolean enabled, Map<String, String> labels, VolMount volMount, Integer replicas, String image, 
                                 Integer containerPort, Resources resources, Probe livenessProbe, Probe readinessProbe,
                                 Boolean rbacEnabled, Boolean secretEnabled, Boolean configMapEnabled, Boolean ingressEnabled,
                                 Ingress ingress, List<Service> services, List<EnvironmentVariable> environment, Boolean initContainerEnabled) {
        this.enabled = enabled;
        this.labels = labels;
        this.volMount = volMount;
        this.replicas = replicas;
        this.image = image;
        this.containerPort = containerPort;
        this.resources = resources;
        this.livenessProbe = livenessProbe;
        this.readinessProbe = readinessProbe;
        this.rbacEnabled = rbacEnabled;
        this.secretEnabled = secretEnabled;
        this.configMapEnabled = configMapEnabled;
        this.ingressEnabled = ingressEnabled;
        this.ingress = ingress;
        this.services = services;
        this.environment = environment;
        this.initContainerEnabled = initContainerEnabled;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Map<String, String> getLabels() {
    return labels;
    }

    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }

    public VolMount getVolMount() {
        return volMount;
    }

    public void setVolMount(VolMount volMount) {
        this.volMount = volMount;
    }

    public Integer getReplicas() {
        return replicas;
    }

    public void setReplicas(Integer replicas) {
        this.replicas = replicas;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Integer getContainerPort() {
        return containerPort;
    }

    public void setContainerPort(Integer containerPort) {
        this.containerPort = containerPort;
    }

    public Resources getResources() {
        return resources;
    }

    public void setResources(Resources resources) {
        this.resources = resources;
    }

    public Probe getLivenessProbe() {
        return livenessProbe;
    }

    public void setLivenessProbe(Probe livenessProbe) {
        this.livenessProbe = livenessProbe;
    }

    public Probe getReadinessProbe() {
        return readinessProbe;
    }

    public void setReadinessProbe(Probe readinessProbe) {
        this.readinessProbe = readinessProbe;
    }

    public Boolean getRbacEnabled() {
        return rbacEnabled;
    }

    public void setRbacEnabled(Boolean rbacEnabled) {
        this.rbacEnabled = rbacEnabled;
    }

    public Boolean getSecretEnabled() {
        return secretEnabled;
    }

    public void setSecretEnabled(Boolean secretEnabled) {
        this.secretEnabled = secretEnabled;
    }

    public Boolean getConfigMapEnabled() {
        return configMapEnabled;
    }

    public void setConfigMapEnabled(Boolean configMapEnabled) {
        this.configMapEnabled = configMapEnabled;
    }

    public Boolean getIngressEnabled() {
        return ingressEnabled;
    }

    public void setIngressEnabled(Boolean ingressEnabled) {
        this.ingressEnabled = ingressEnabled;
    }

    public Ingress getIngress() {
        return ingress;
    }

    public void setIngress(Ingress ingress) {
        this.ingress = ingress;
    }

    public List<Service> getServices() {
        return services;
    }

    public void setServices(List<Service> services) {
        this.services = services;
    }

    public List<EnvironmentVariable> getEnvironment() {
        return environment;
    }

    public void setEnvironment(List<EnvironmentVariable> environment) {
        this.environment = environment;
    }

    public Boolean getInitContainerEnabled() {
        return initContainerEnabled;
    }

    public void setInitContainerEnabled(Boolean initContainerEnabled) {
        this.initContainerEnabled = initContainerEnabled;
    }


    @Override
    public String toString() {
        return "PaymentHubDeploymentSpec{" +
                "enabled=" + enabled +
                ", labels=" + labels +
                ", volMount=" + volMount +
                ", replicas=" + replicas +
                ", image='" + image + '\'' +
                ", containerPort=" + containerPort +
                ", resources=" + resources +
                ", livenessProbe=" + livenessProbe +
                ", readinessProbe=" + readinessProbe +
                ", rbacEnabled=" + rbacEnabled +
                ", secretEnabled=" + secretEnabled +
                ", configMapEnabled=" + configMapEnabled +
                ", ingressEnabled=" + ingressEnabled +
                ", ingress=" + ingress +
                ", services=" + services +
                ", environment=" + environment +
                ", initContainerEnabled=" + initContainerEnabled +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PaymentHubDeploymentSpec)) return false;
        PaymentHubDeploymentSpec that = (PaymentHubDeploymentSpec) o;
        return Objects.equals(getEnabled(), that.getEnabled()) &&
               Objects.equals(getLabels(), that.getLabels()) && 
               Objects.equals(getVolMount(), that.getVolMount()) &&
               Objects.equals(getReplicas(), that.getReplicas()) &&
               Objects.equals(getImage(), that.getImage()) &&
               Objects.equals(getContainerPort(), that.getContainerPort()) &&
               Objects.equals(getResources(), that.getResources()) &&
               Objects.equals(getLivenessProbe(), that.getLivenessProbe()) &&
               Objects.equals(getReadinessProbe(), that.getReadinessProbe()) &&
               Objects.equals(getRbacEnabled(), that.getRbacEnabled()) &&
               Objects.equals(getSecretEnabled(), that.getSecretEnabled()) &&
               Objects.equals(getConfigMapEnabled(), that.getConfigMapEnabled()) &&
               Objects.equals(getIngressEnabled(), that.getIngressEnabled()) &&
               Objects.equals(getIngress(), that.getIngress()) &&
               Objects.equals(getServices(), that.getServices()) &&
               Objects.equals(getEnvironment(), that.getEnvironment()) &&
               Objects.equals(getInitContainerEnabled(), that.getInitContainerEnabled());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEnabled(), getLabels(), getVolMount(), getReplicas(), getImage(), getContainerPort(), 
                            getResources(), getLivenessProbe(), getReadinessProbe(), getRbacEnabled(), getSecretEnabled(), 
                            getConfigMapEnabled(), getIngressEnabled(), getIngress(), getServices(), getEnvironment(), getInitContainerEnabled());
    }

    // Inner classes for nested objects 
    public static class VolMount {
        private Boolean enabled;
        private String name;

        public VolMount() {
        }

        public VolMount(Boolean enabled, String name) {
            this.enabled = enabled;
            this.name = name;
        }

        public Boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "VolMount{" +
                    "enabled=" + enabled +
                    ", name='" + name + '\'' +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof VolMount)) return false;
            VolMount volMount = (VolMount) o;
            return Objects.equals(enabled, volMount.enabled) &&
                Objects.equals(name, volMount.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(enabled, name);
        }
    }

    public static class Resources {
        private ResourceDetails limits;
        private ResourceDetails requests;

        public Resources() {
        }

        public Resources(ResourceDetails limits, ResourceDetails requests) {
            this.limits = limits;
            this.requests = requests;
        }

        public ResourceDetails getLimits() {
            return limits;
        }

        public void setLimits(ResourceDetails limits) {
            this.limits = limits;
        }

        public ResourceDetails getRequests() {
            return requests;
        }

        public void setRequests(ResourceDetails requests) {
            this.requests = requests;
        }

        @Override
        public String toString() {
            return "Resources{" +
                    "limits=" + limits +
                    ", requests=" + requests +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Resources)) return false;
            Resources that = (Resources) o;
            return Objects.equals(getLimits(), that.getLimits()) &&
                   Objects.equals(getRequests(), that.getRequests());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getLimits(), getRequests());
        }
    }

    public static class ResourceDetails {
        private String cpu;
        private String memory;

        public ResourceDetails() {
        }

        public ResourceDetails(String cpu, String memory) {
            this.cpu = cpu;
            this.memory = memory;
        }

        public String getCpu() {
            return cpu;
        }

        public void setCpu(String cpu) {
            this.cpu = cpu;
        }

        public String getMemory() {
            return memory;
        }

        public void setMemory(String memory) {
            this.memory = memory;
        }

        @Override
        public String toString() {
            return "ResourceDetails{" +
                    "cpu='" + cpu + '\'' +
                    ", memory='" + memory + '\'' +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ResourceDetails)) return false;
            ResourceDetails that = (ResourceDetails) o;
            return Objects.equals(getCpu(), that.getCpu()) &&
                   Objects.equals(getMemory(), that.getMemory());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getCpu(), getMemory());
        }
    }

    public static class Probe {
        private String path;
        private Integer port;
        private Integer initialDelaySeconds;
        private Integer periodSeconds;
        private Integer failureThreshold;
        private Integer timeoutSeconds;

        public Probe() {
        }

        public Probe(String path, Integer port, Integer initialDelaySeconds,
                     Integer periodSeconds, Integer failureThreshold, Integer timeoutSeconds) {
            this.path = path;
            this.port = port;
            this.initialDelaySeconds = initialDelaySeconds;
            this.periodSeconds = periodSeconds;
            this.failureThreshold = failureThreshold;
            this.timeoutSeconds = timeoutSeconds;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public Integer getPort() {
            return port;
        }

        public void setPort(Integer port) {
            this.port = port;
        }

        public Integer getInitialDelaySeconds() {
            return initialDelaySeconds;
        }

        public void setInitialDelaySeconds(Integer initialDelaySeconds) {
            this.initialDelaySeconds = initialDelaySeconds;
        }

        public Integer getPeriodSeconds() {
            return periodSeconds;
        }

        public void setPeriodSeconds(Integer periodSeconds) {
            this.periodSeconds = periodSeconds;
        }

        public Integer getFailureThreshold() {
            return failureThreshold;
        }

        public void setFailureThreshold(Integer failureThreshold) {
            this.failureThreshold = failureThreshold;
        }

        public Integer getTimeoutSeconds() {
            return timeoutSeconds;
        }

        public void setTimeoutSeconds(Integer timeoutSeconds) {
            this.timeoutSeconds = timeoutSeconds;
        }

        @Override
        public String toString() {
            return "Probe{" +
                    "path='" + path + '\'' +
                    ", port=" + port +
                    ", initialDelaySeconds=" + initialDelaySeconds +
                    ", periodSeconds=" + periodSeconds +
                    ", failureThreshold=" + failureThreshold +
                    ", timeoutSeconds=" + timeoutSeconds +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Probe)) return false;
            Probe that = (Probe) o;
            return Objects.equals(getPath(), that.getPath()) &&
                   Objects.equals(getPort(), that.getPort()) &&
                   Objects.equals(getInitialDelaySeconds(), that.getInitialDelaySeconds()) &&
                   Objects.equals(getPeriodSeconds(), that.getPeriodSeconds()) &&
                   Objects.equals(getFailureThreshold(), that.getFailureThreshold()) &&
                   Objects.equals(getTimeoutSeconds(), that.getTimeoutSeconds());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getPath(), getPort(), getInitialDelaySeconds(),
                                getPeriodSeconds(), getFailureThreshold(), getTimeoutSeconds());
        }
    }
    
    public static class Ingress {
        private String host;
        private String path;
        private String className;
        private Map<String, String> annotations;
        private List<TLS> tls;
        private List<Rule> rules;
        private Map<String, String> labels;

        public Ingress() {
        }

        public Ingress(String host, String path, String className, Map<String, String> annotations, List<TLS> tls, List<Rule> rules) {
            this.host = host;
            this.path = path;
            this.className = className;
            this.annotations = annotations;
            this.tls = tls;
            this.rules = rules;
            this.labels = labels;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public Map<String, String> getAnnotations() {
            return annotations;
        }

        public void setAnnotations(Map<String, String> annotations) {
            this.annotations = annotations;
        }

        public List<TLS> getTls() {
            return tls;
        }

        public void setTls(List<TLS> tls) {
            this.tls = tls;
        }

        public List<Rule> getRules() {
            return rules;
        }

        public void setRules(List<Rule> rules) {
            this.rules = rules;
        }

        public Map<String, String> getLabels() {
            return labels;
        }

        public void setLabels(Map<String, String> labels) {
            this.labels = labels;
        }        

        @Override
        public String toString() {
            return "Ingress{" +
                    "host='" + host + '\'' +
                    ", path='" + path + '\'' +
                    ", className='" + className + '\'' +
                    ", annotations=" + annotations +
                    ", tls=" + tls +
                    ", rules=" + rules +
                    ", labels=" + labels +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Ingress)) return false;
            Ingress ingress = (Ingress) o;
            return Objects.equals(getHost(), ingress.getHost()) &&
                Objects.equals(getPath(), ingress.getPath()) &&
                Objects.equals(getClassName(), ingress.getClassName()) &&
                Objects.equals(getAnnotations(), ingress.getAnnotations()) &&
                Objects.equals(getTls(), ingress.getTls()) &&
                Objects.equals(getRules(), ingress.getRules()) &&
                Objects.equals(getLabels(), ingress.getLabels());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getHost(), getPath(), getClassName(), getAnnotations(), getTls(), getRules(), getLabels());
        }

        // Inner class for TLS settings
        public static class TLS {
            private List<String> hosts;
            private String secretName;

            public TLS() {
            }

            public TLS(List<String> hosts, String secretName) {
                this.hosts = hosts;
                this.secretName = secretName;
            }

            public List<String> getHosts() {
                return hosts;
            }

            public void setHosts(List<String> hosts) {
                this.hosts = hosts;
            }

            public String getSecretName() {
                return secretName;
            }

            public void setSecretName(String secretName) {
                this.secretName = secretName;
            }

            @Override
            public String toString() {
                return "TLS{" +
                        "hosts=" + hosts +
                        ", secretName='" + secretName + '\'' +
                        '}';
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (!(o instanceof TLS)) return false;
                TLS tls = (TLS) o;
                return Objects.equals(getHosts(), tls.getHosts()) &&
                    Objects.equals(getSecretName(), tls.getSecretName());
            }

            @Override
            public int hashCode() {
                return Objects.hash(getHosts(), getSecretName());
            }
        }

        // Inner class for Rule settings
        public static class Rule {
            private String host;
            private List<Path> paths;

            public Rule() {
            }

            public Rule(String host, List<Path> paths) {
                this.host = host;
                this.paths = paths;
            }

            public String getHost() {
                return host;
            }

            public void setHost(String host) {
                this.host = host;
            }

            public List<Path> getPaths() {
                return paths;
            }

            public void setPaths(List<Path> paths) {
                this.paths = paths;
            }

            @Override
            public String toString() {
                return "Rule{" +
                        "host='" + host + '\'' +
                        ", paths=" + paths +
                        '}';
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (!(o instanceof Rule)) return false;
                Rule rule = (Rule) o;
                return Objects.equals(getHost(), rule.getHost()) &&
                    Objects.equals(getPaths(), rule.getPaths());
            }

            @Override
            public int hashCode() {
                return Objects.hash(getHost(), getPaths());
            }
        }

        // Inner class for Path settings
        public static class Path {
            private String path;
            private String pathType;
            private Backend backend;

            public Path() {
            }

            public Path(String path, String pathType, Backend backend) {
                this.path = path;
                this.pathType = pathType;
                this.backend = backend;
            }

            public String getPath() {
                return path;
            }

            public void setPath(String path) {
                this.path = path;
            }

            public String getPathType() {
                return pathType;
            }

            public void setPathType(String pathType) {
                this.pathType = pathType;
            }

            public Backend getBackend() {
                return backend;
            }

            public void setBackend(Backend backend) {
                this.backend = backend;
            }

            @Override
            public String toString() {
                return "Path{" +
                        "path='" + path + '\'' +
                        ", pathType='" + pathType + '\'' +
                        ", backend=" + backend +
                        '}';
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (!(o instanceof Path)) return false;
                Path path1 = (Path) o;
                return Objects.equals(getPath(), path1.getPath()) &&
                    Objects.equals(getPathType(), path1.getPathType()) &&
                    Objects.equals(getBackend(), path1.getBackend());
            }

            @Override
            public int hashCode() {
                return Objects.hash(getPath(), getPathType(), getBackend());
            }
        }

        // Inner class for Backend settings
        public static class Backend {
            private Service service;

            public Backend() {
            }

            public Backend(Service service) {
                this.service = service;
            }

            public Service getService() {
                return service;
            }

            public void setService(Service service) {
                this.service = service;
            }

            @Override
            public String toString() {
                return "Backend{" +
                        "service=" + service +
                        '}';
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (!(o instanceof Backend)) return false;
                Backend backend = (Backend) o;
                return Objects.equals(getService(), backend.getService());
            }

            @Override
            public int hashCode() {
                return Objects.hash(getService());
            }
        }

        // Inner class for Service settings
        public static class Service {
            private String name;
            private Port port;

            public Service() {
            }

            public Service(String name, Port port) {
                this.name = name;
                this.port = port;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public Port getPort() {
                return port;
            }

            public void setPort(Port port) {
                this.port = port;
            }

            @Override
            public String toString() {
                return "Service{" +
                        "name='" + name + '\'' +
                        ", port=" + port +
                        '}';
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (!(o instanceof Service)) return false;
                Service service = (Service) o;
                return Objects.equals(getName(), service.getName()) &&
                    Objects.equals(getPort(), service.getPort());
            }

            @Override
            public int hashCode() {
                return Objects.hash(getName(), getPort());
            }
        }

        // Inner class for Port settings
        public static class Port {
            private Integer number;

            public Port() {
            }

            public Port(Integer number) {
                this.number = number;
            }

            public Integer getNumber() {
                return number;
            }

            public void setNumber(Integer number) {
                this.number = number;
            }

            @Override
            public String toString() {
                return "Port{" +
                        "number=" + number +
                        '}';
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (!(o instanceof Port)) return false;
                Port port = (Port) o;
                return Objects.equals(getNumber(), port.getNumber());
            }

            @Override
            public int hashCode() {
                return Objects.hash(getNumber());
            }
        }
    }

    public static class Service {
        private String name;
        private List<Port> ports;
        private Map<String, String> selector;
        private String type;
        private Map<String, String> annotations;
        private String sessionAffinity;
        private Map<String, String> labels;  

        public Service() {
        }

        private Service(Builder builder) {
            this.name = builder.name;
            this.ports = builder.ports;
            this.selector = builder.selector;
            this.type = builder.type;
            this.annotations = builder.annotations;
            this.sessionAffinity = builder.sessionAffinity;
            this.labels = builder.labels;  
        }

        // Getters for all fields
        public Map<String, String> getLabels() {
            return labels;
        }

        public void setLabels(Map<String, String> labels) {
            this.labels = labels;
        }
        
        public String getName() {
            return name;
        }

        public List<Port> getPorts() {
            return ports;
        }

        public Map<String, String> getSelector() {
            return selector;
        }

        public String getType() {
            return type;
        }

        public Map<String, String> getAnnotations() {
            return annotations;
        }

        public String getSessionAffinity() {
            return sessionAffinity;
        }

        @Override
        public String toString() {
            return "Service{" +
                    "name='" + name + '\'' +
                    ", ports=" + ports +
                    ", selector=" + selector +
                    ", type='" + type + '\'' +
                    ", annotations=" + annotations +
                    ", sessionAffinity='" + sessionAffinity + '\'' +
                    ", labels=" + labels +  
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Service)) return false;
            Service service = (Service) o;
            return Objects.equals(name, service.name) &&
                Objects.equals(ports, service.ports) &&
                Objects.equals(selector, service.selector) &&
                Objects.equals(type, service.type) &&
                Objects.equals(annotations, service.annotations) &&
                Objects.equals(sessionAffinity, service.sessionAffinity) &&
                Objects.equals(labels, service.labels);  
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, ports, selector, type, annotations, sessionAffinity, labels);
        }

        // Builder class for Service
        @JsonPOJOBuilder(withPrefix = "")
        public static class Builder {
            private String name;
            private List<Port> ports;
            private Map<String, String> selector = new HashMap<>();
            private String type;
            private Map<String, String> annotations = new HashMap<>();
            private String sessionAffinity;
            private Map<String, String> labels = new HashMap<>();

            public Builder withName(String name) {
                this.name = name;
                return this;
            }

            public Builder withPorts(List<Port> ports) {
                this.ports = ports;
                return this;
            }

            public Builder withSelector(Map<String, String> selector) {
                this.selector = selector;
                return this;
            }

            public Builder withType(String type) {
                this.type = type;
                return this;
            }

            public Builder withAnnotations(Map<String, String> annotations) {
                this.annotations = annotations;
                return this;
            }

            public Builder withSessionAffinity(String sessionAffinity) {
                this.sessionAffinity = sessionAffinity;
                return this;
            }

            public Builder withLabels(Map<String, String> labels) {
                this.labels = labels;
                return this;
            }

            public Service build() {
                return new Service(this);
            }
        }

        // Nested class for Port
        public static class Port {
            private String name;
            private Integer port;
            private Integer targetPort;
            private String protocol;

            // Default constructor required for Jackson
            public Port() {
            }

            // Private constructor to enforce builder usage
            private Port(PortBuilder builder) {
                this.name = builder.name;
                this.port = builder.port;
                this.targetPort = builder.targetPort;
                this.protocol = builder.protocol;
            }

            // Getters for all fields
            public String getName() {
                return name;
            }

            public Integer getPort() {
                return port;
            }

            public Integer getTargetPort() {
                return targetPort;
            }

            public String getProtocol() {
                return protocol;
            }

            @Override
            public String toString() {
                return "Port{" +
                        "name='" + name + '\'' +
                        ", port=" + port +
                        ", targetPort=" + targetPort +
                        ", protocol='" + protocol + '\'' +
                        '}';
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (!(o instanceof Port)) return false;
                Port port1 = (Port) o;
                return Objects.equals(name, port1.name) &&
                       Objects.equals(port, port1.port) &&
                       Objects.equals(targetPort, port1.targetPort) &&
                       Objects.equals(protocol, port1.protocol);
            }

            @Override
            public int hashCode() {
                return Objects.hash(name, port, targetPort, protocol);
            }

            // Builder class for Port
            @JsonPOJOBuilder(withPrefix = "")
            public static class PortBuilder {
                private String name;
                private Integer port;
                private Integer targetPort;
                private String protocol;

                public PortBuilder withName(String name) {
                    this.name = name;
                    return this;
                }

                public PortBuilder withPort(Integer port) {
                    this.port = port;
                    return this;
                }

                public PortBuilder withTargetPort(Integer targetPort) {
                    this.targetPort = targetPort;
                    return this;
                }

                public PortBuilder withProtocol(String protocol) {
                    this.protocol = protocol;
                    return this;
                }

                public Port build() {
                    return new Port(this);
                }
            }
        }
    }

    public static class EnvironmentVariable {
        private String name;
        private String value;  // Using String type for value
        private ValueFrom valueFrom;  // New field to support valueFrom

        public EnvironmentVariable() {
        }

        public EnvironmentVariable(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public EnvironmentVariable(String name, String value, ValueFrom valueFrom) {
            this.name = name;
            this.value = value;
            this.valueFrom = valueFrom;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public ValueFrom getValueFrom() {
            return valueFrom;
        }

        public void setValueFrom(ValueFrom valueFrom) {
            this.valueFrom = valueFrom;
        }

        @Override
        public String toString() {
            return "EnvironmentVariable{" +
                    "name='" + name + '\'' +
                    ", value=" + value +
                    ", valueFrom=" + valueFrom +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof EnvironmentVariable)) return false;
            EnvironmentVariable that = (EnvironmentVariable) o;
            return Objects.equals(getName(), that.getName()) &&
                    Objects.equals(getValue(), that.getValue()) &&
                    Objects.equals(getValueFrom(), that.getValueFrom());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getName(), getValue(), getValueFrom());
        }

        // Inner class ValueFrom
        public static class ValueFrom {
            private SecretKeyRef secretKeyRef;

            public ValueFrom() {
            }

            public ValueFrom(SecretKeyRef secretKeyRef) {
                this.secretKeyRef = secretKeyRef;
            }

            public SecretKeyRef getSecretKeyRef() {
                return secretKeyRef;
            }

            public void setSecretKeyRef(SecretKeyRef secretKeyRef) {
                this.secretKeyRef = secretKeyRef;
            }

            @Override
            public String toString() {
                return "ValueFrom{" +
                        "secretKeyRef=" + secretKeyRef +
                        '}';
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (!(o instanceof ValueFrom)) return false;
                ValueFrom that = (ValueFrom) o;
                return Objects.equals(getSecretKeyRef(), that.getSecretKeyRef());
            }

            @Override
            public int hashCode() {
                return Objects.hash(getSecretKeyRef());
            }

            // Inner class SecretKeyRef
            public static class SecretKeyRef {
                private String name;
                private String key;

                public SecretKeyRef() {
                }

                public SecretKeyRef(String name, String key) {
                    this.name = name;
                    this.key = key;
                }

                public String getName() {
                    return name;
                }

                public void setName(String name) {
                    this.name = name;
                }

                public String getKey() {
                    return key;
                }

                public void setKey(String key) {
                    this.key = key;
                }

                @Override
                public String toString() {
                    return "SecretKeyRef{" +
                            "name='" + name + '\'' +
                            ", key='" + key + '\'' +
                            '}';
                }

                @Override
                public boolean equals(Object o) {
                    if (this == o) return true;
                    if (!(o instanceof SecretKeyRef)) return false;
                    SecretKeyRef that = (SecretKeyRef) o;
                    return Objects.equals(getName(), that.getName()) &&
                            Objects.equals(getKey(), that.getKey());
                }

                @Override
                public int hashCode() {
                    return Objects.hash(getName(), getKey());
                }
            }
        }
    }

}
