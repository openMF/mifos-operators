package com.example.operator;

public class PhEeImporterRdbmsSpec {

    private String image;
    private int replicas;
    private String springProfilesActive;
    private Datasource datasource;
    private Resources resources;
    private Logging logging;
    private String javaToolOptions;
    private String bucketName;

    // Getters and Setters

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getReplicas() {
        return replicas;
    }

    public void setReplicas(int replicas) {
        this.replicas = replicas;
    }

    public String getSpringProfilesActive() {
        return springProfilesActive;
    }

    public void setSpringProfilesActive(String springProfilesActive) {
        this.springProfilesActive = springProfilesActive;
    }

    public Datasource getDatasource() {
        return datasource;
    }

    public void setDatasource(Datasource datasource) {
        this.datasource = datasource;
    }

    public Resources getResources() {
        return resources;
    }

    public void setResources(Resources resources) {
        this.resources = resources;
    }

    public Logging getLogging() {
        return logging;
    }

    public void setLogging(Logging logging) {
        this.logging = logging;
    }

    public String getJavaToolOptions() {
        return javaToolOptions;
    }

    public void setJavaToolOptions(String javaToolOptions) {
        this.javaToolOptions = javaToolOptions;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public static class Datasource {
        private String username;
        private String password;
        private String host;
        private int port;
        private String schema;

        // Getters and Setters
        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getSchema() {
            return schema;
        }

        public void setSchema(String schema) {
            this.schema = schema;
        }
    }

    public static class Resources {
        private Limits limits;
        private Requests requests;

        // Getters and Setters
        public Limits getLimits() {
            return limits;
        }

        public void setLimits(Limits limits) {
            this.limits = limits;
        }

        public Requests getRequests() {
            return requests;
        }

        public void setRequests(Requests requests) {
            this.requests = requests;
        }

        public static class Limits {
            private String cpu;
            private String memory;

            // Getters and Setters
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
        }

        public static class Requests {
            private String cpu;
            private String memory;

            // Getters and Setters
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
        }
    }

    public static class Logging {
        private String levelRoot;
        private String patternConsole;

        // Getters and Setters
        public String getLevelRoot() {
            return levelRoot;
        }

        public void setLevelRoot(String levelRoot) {
            this.levelRoot = levelRoot;
        }

        public String getPatternConsole() {
            return patternConsole;
        }

        public void setPatternConsole(String patternConsole) {
            this.patternConsole = patternConsole;
        }
    }
}
