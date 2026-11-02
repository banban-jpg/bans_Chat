package egovframework.example.global.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

@Component
public class ProfileBoundaryGuard implements BeanFactoryPostProcessor, EnvironmentAware {
    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        boolean oracle = active("oracle");
        boolean oracleIt = active("oracle-it");
        boolean test = active("test");
        boolean localMock = active("local-mock");
        if ((oracle && (test || localMock)) || (test && localMock)) {
            throw new IllegalStateException("PROFILE_CONFLICT: database and mock/test profiles must be separate");
        }
        if (oracleIt && !oracle) {
            throw new IllegalStateException("PROFILE_CONFLICT: oracle-it requires oracle");
        }
    }

    private boolean active(String name) {
        return environment.acceptsProfiles(Profiles.of(name));
    }
}
