package io.github.girisenji.ai.aura.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for Aura Gateway
 */
@Configuration
@ConfigurationProperties(prefix = "aura")
public class AuraProperties {
    
    private Providers providers = new Providers();
    private Classifier classifier = new Classifier();
    private RateLimit rateLimit = new RateLimit();
    private CostTracking costTracking = new CostTracking();
    private Guardrails guardrails = new Guardrails();
    
    // Getters and Setters
    public Providers getProviders() {
        return providers;
    }
    
    public void setProviders(Providers providers) {
        this.providers = providers;
    }
    
    public Classifier getClassifier() {
        return classifier;
    }
    
    public void setClassifier(Classifier classifier) {
        this.classifier = classifier;
    }
    
    public RateLimit getRateLimit() {
        return rateLimit;
    }
    
    public void setRateLimit(RateLimit rateLimit) {
        this.rateLimit = rateLimit;
    }
    
    public CostTracking getCostTracking() {
        return costTracking;
    }
    
    public void setCostTracking(CostTracking costTracking) {
        this.costTracking = costTracking;
    }
    
    public Guardrails getGuardrails() {
        return guardrails;
    }
    
    public void setGuardrails(Guardrails guardrails) {
        this.guardrails = guardrails;
    }
    
    public static class Providers {
        private OpenAI openai = new OpenAI();
        private Anthropic anthropic = new Anthropic();
        private Azure azure = new Azure();
        private Ollama ollama = new Ollama();
        
        public OpenAI getOpenai() {
            return openai;
        }
        
        public void setOpenai(OpenAI openai) {
            this.openai = openai;
        }
        
        public Anthropic getAnthropic() {
            return anthropic;
        }
        
        public void setAnthropic(Anthropic anthropic) {
            this.anthropic = anthropic;
        }
        
        public Azure getAzure() {
            return azure;
        }
        
        public void setAzure(Azure azure) {
            this.azure = azure;
        }
        
        public Ollama getOllama() {
            return ollama;
        }
        
        public void setOllama(Ollama ollama) {
            this.ollama = ollama;
        }
    }
    
    public static class OpenAI {
        private String apiKey;
        private String baseUrl;
        private Duration timeout;
        private int maxRetries;
        private Models models = new Models();
        
        public String getApiKey() {
            return apiKey;
        }
        
        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }
        
        public String getBaseUrl() {
            return baseUrl;
        }
        
        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }
        
        public Duration getTimeout() {
            return timeout;
        }
        
        public void setTimeout(Duration timeout) {
            this.timeout = timeout;
        }
        
        public int getMaxRetries() {
            return maxRetries;
        }
        
        public void setMaxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
        }
        
        public Models getModels() {
            return models;
        }
        
        public void setModels(Models models) {
            this.models = models;
        }
        
        public static class Models {
            private String premium;
            private String balanced;
            private String eco;
            
            public String getPremium() {
                return premium;
            }
            
            public void setPremium(String premium) {
                this.premium = premium;
            }
            
            public String getBalanced() {
                return balanced;
            }
            
            public void setBalanced(String balanced) {
                this.balanced = balanced;
            }
            
            public String getEco() {
                return eco;
            }
            
            public void setEco(String eco) {
                this.eco = eco;
            }
        }
    }
    
    public static class Anthropic {
        private String apiKey;
        private String baseUrl;
        private Duration timeout;
        private int maxRetries;
        private Models models = new Models();
        
        public String getApiKey() {
            return apiKey;
        }
        
        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }
        
        public String getBaseUrl() {
            return baseUrl;
        }
        
        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }
        
        public Duration getTimeout() {
            return timeout;
        }
        
        public void setTimeout(Duration timeout) {
            this.timeout = timeout;
        }
        
        public int getMaxRetries() {
            return maxRetries;
        }
        
        public void setMaxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
        }
        
        public Models getModels() {
            return models;
        }
        
        public void setModels(Models models) {
            this.models = models;
        }
        
        public static class Models {
            private String premium;
            private String balanced;
            private String eco;
            
            public String getPremium() {
                return premium;
            }
            
            public void setPremium(String premium) {
                this.premium = premium;
            }
            
            public String getBalanced() {
                return balanced;
            }
            
            public void setBalanced(String balanced) {
                this.balanced = balanced;
            }
            
            public String getEco() {
                return eco;
            }
            
            public void setEco(String eco) {
                this.eco = eco;
            }
        }
    }
    
    public static class Azure {
        private String apiKey;
        private String endpoint;
        private String deploymentName;
        private String apiVersion;
        private Duration timeout;
        
        public String getApiKey() {
            return apiKey;
        }
        
        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }
        
        public String getEndpoint() {
            return endpoint;
        }
        
        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }
        
        public String getDeploymentName() {
            return deploymentName;
        }
        
        public void setDeploymentName(String deploymentName) {
            this.deploymentName = deploymentName;
        }
        
        public String getApiVersion() {
            return apiVersion;
        }
        
        public void setApiVersion(String apiVersion) {
            this.apiVersion = apiVersion;
        }
        
        public Duration getTimeout() {
            return timeout;
        }
        
        public void setTimeout(Duration timeout) {
            this.timeout = timeout;
        }
    }
    
    public static class Ollama {
        private String baseUrl;
        private Duration timeout;
        private Models models = new Models();
        
        public String getBaseUrl() {
            return baseUrl;
        }
        
        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }
        
        public Duration getTimeout() {
            return timeout;
        }
        
        public void setTimeout(Duration timeout) {
            this.timeout = timeout;
        }
        
        public Models getModels() {
            return models;
        }
        
        public void setModels(Models models) {
            this.models = models;
        }
        
        public static class Models {
            private String defaultModel;
            
            public String getDefaultModel() {
                return defaultModel;
            }
            
            public void setDefaultModel(String defaultModel) {
                this.defaultModel = defaultModel;
            }
        }
    }
    
    public static class Classifier {
        private String modelPath;
        private Threshold threshold = new Threshold();
        
        public String getModelPath() {
            return modelPath;
        }
        
        public void setModelPath(String modelPath) {
            this.modelPath = modelPath;
        }
        
        public Threshold getThreshold() {
            return threshold;
        }
        
        public void setThreshold(Threshold threshold) {
            this.threshold = threshold;
        }
        
        public static class Threshold {
            private double eco;
            private double balanced;
            private double premium;
            
            public double getEco() {
                return eco;
            }
            
            public void setEco(double eco) {
                this.eco = eco;
            }
            
            public double getBalanced() {
                return balanced;
            }
            
            public void setBalanced(double balanced) {
                this.balanced = balanced;
            }
            
            public double getPremium() {
                return premium;
            }
            
            public void setPremium(double premium) {
                this.premium = premium;
            }
        }
    }
    
    public static class RateLimit {
        private boolean enabled;
        private int defaultLimit;
        private Duration window;
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public int getDefaultLimit() {
            return defaultLimit;
        }
        
        public void setDefaultLimit(int defaultLimit) {
            this.defaultLimit = defaultLimit;
        }
        
        public Duration getWindow() {
            return window;
        }
        
        public void setWindow(Duration window) {
            this.window = window;
        }
    }
    
    public static class CostTracking {
        private boolean enabled;
        private String kafkaTopic;
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public String getKafkaTopic() {
            return kafkaTopic;
        }
        
        public void setKafkaTopic(String kafkaTopic) {
            this.kafkaTopic = kafkaTopic;
        }
    }
    
    public static class Guardrails {
        private PiiMasking piiMasking = new PiiMasking();
        private ContentModeration contentModeration = new ContentModeration();
        
        public PiiMasking getPiiMasking() {
            return piiMasking;
        }
        
        public void setPiiMasking(PiiMasking piiMasking) {
            this.piiMasking = piiMasking;
        }
        
        public ContentModeration getContentModeration() {
            return contentModeration;
        }
        
        public void setContentModeration(ContentModeration contentModeration) {
            this.contentModeration = contentModeration;
        }
        
        public static class PiiMasking {
            private boolean enabled;
            
            public boolean isEnabled() {
                return enabled;
            }
            
            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }
        }
        
        public static class ContentModeration {
            private boolean enabled;
            
            public boolean isEnabled() {
                return enabled;
            }
            
            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }
        }
    }
}
