package fr.insee.genesis.configuration;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;

import java.util.*;
import java.util.function.Supplier;

@Slf4j
public class PropertiesLogger implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    public static final String PROPERTY_KEY_FOR_PREFIXES = "fr.insee.properties.log.key.prefixes";
    public static final String PROPERTY_KEY_FOR_MORE_HIDDEN = "fr.insee.properties.log.key.hidden.more";
    public static final String PROPERTY_KEY_FOR_SOURCES_IGNORED = "fr.insee.properties.log.sources.ignored";
    public static final String PROPERTY_KEY_FOR_SOURCES_SELECT = "fr.insee.properties.log.key.select";
    private static final Set<String> hiddenWords = Set.of("password", "pwd", "jeton", "token", "secret", "credential", "pw");
    private static final Set<String> defaultPrintPrefixes= Set.of("fr.insee","logging","keycloak","spring","application","server","springdoc","management");
    private static final Set<String> defaultIgnoredPropertySources = Set.of("systemProperties", "systemEnvironment");
    public static final PropertySelectorEnum DEFAULT_PROPERTY_SELECTOR = PropertySelectorEnum.PREFIX;
    private static Set<String> prefixForSelectedProps;

    private final Collection<String> propertySourceNames=new ArrayList<>();
    private Set<String> hiddensProps;
    private Set<String> ignoredPropertySources;
    private PropertySelector propertySelector;

    @Override
    public void onApplicationEvent(@NonNull ApplicationEnvironmentPreparedEvent event) {
        Environment environment=event.getEnvironment();

        var props= new StringBuilder();
        this.hiddensProps = getMoreHiddenPropsFromPropertyAndMerge(environment);
        prefixForSelectedProps = environment.getProperty(PROPERTY_KEY_FOR_PREFIXES, Set.class, defaultPrintPrefixes);
        this.ignoredPropertySources = environment.getProperty(PROPERTY_KEY_FOR_SOURCES_IGNORED, Set.class, defaultIgnoredPropertySources);
        var propertySelectorType=this.getSelectorFromProperty(environment.getProperty(PROPERTY_KEY_FOR_SOURCES_SELECT))
                                     .orElse(DEFAULT_PROPERTY_SELECTOR);
        log.atDebug().log(()->"Logging "+propertySelectorType.forLogging());
        this.propertySelector=propertySelectorType.propertySelector();

        ((AbstractEnvironment) environment).getPropertySources().stream()
                .filter(this::isEnumerable)
                .filter(this::sourceWillBeProcessed)
                .map(this::rememberPropertySourceNameThenCast)
                .map(EnumerablePropertySource::getPropertyNames)
                .flatMap(Arrays::stream)
                .distinct()
                .filter(Objects::nonNull)
                .filter(this::filterFromPropertySelector)
                .forEach(key-> props.append(key).append(" = ")
                        .append(resoutValeurAvecMasquePwd(key, environment))
                        .append(System.lineSeparator()));
        props.append("============================================================================");
        props.insert(0, """
                ===============================================================================================
                                                Properties :
                %s                                        
                ===============================================================================================
                """.formatted(this.propertySourceNames.stream().reduce("",(l, e)->l+System.lineSeparator()+"- "+e )));
        log.info(props.toString());

    }

    private static Set<String> getMoreHiddenPropsFromPropertyAndMerge(Environment environment) {
        var moreProps = environment.getProperty(PROPERTY_KEY_FOR_MORE_HIDDEN, Set.class);
        var retour = hiddenWords;
        if (moreProps != null){
            retour=new HashSet<>(moreProps);
            retour.addAll(hiddenWords);
        }
        return retour;
    }

    private Optional<PropertySelectorEnum> getSelectorFromProperty(String property) {
        if(property!=null){
            try{
                return Optional.of(PropertySelectorEnum.valueOf(property));
            }catch (IllegalArgumentException ie){
                log.atTrace().log(()->"Impossible de convertir "+property+" en une constante de PropertySelectorEnum. Le PropertySelector par défaut sera utilisé.");
            }
        }
        return Optional.empty();
    }

    private boolean filterFromPropertySelector(@NonNull String s) {
        if (! this.propertySelector.filter(s)){
            log.atDebug().log(()->s+ " ne commence pas par un des prefix retenus pour être loguée");
            return false;
        }
        return true;
    }

    private boolean sourceWillBeProcessed(PropertySource<?> propertySource) {

        if (ignoredPropertySources.contains(propertySource.getName())){
            log.atDebug().log(()->propertySource+ " sera ignorée");
            return false;
        }
        return true;
    }

    private EnumerablePropertySource<?> rememberPropertySourceNameThenCast(PropertySource<?> propertySource) {
        this.propertySourceNames.add(propertySource.getName());
        return (EnumerablePropertySource<?>) propertySource;
    }

    private boolean isEnumerable(PropertySource<?> propertySource) {
        if (! (propertySource instanceof EnumerablePropertySource)){
            log.atDebug().log(()->propertySource+ " n'est pas EnumerablePropertySource : impossible à lister");
            return false;
        }
        return true;
    }

    private Object resoutValeurAvecMasquePwd(String key, Environment environment) {
        if (hiddensProps.stream().anyMatch(key::contains)) {
            return "******";
        }
        return environment.getProperty(key);

    }


    @FunctionalInterface
    private interface PropertySelector {
        boolean filter(String s);
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    private enum PropertySelectorEnum {
        ALL(s->true, ()->"all properties"),
        NONE(s->false, ()->"no properties"),
        PREFIX(k->prefixForSelectedProps.stream().anyMatch(k::startsWith), () -> "properties starting with "+ prefixForSelectedProps);

        private final PropertySelector propertySelector;
        private final Supplier<String> logString;

        public PropertySelector propertySelector() {
            return propertySelector;
        }

        public String forLogging(){
            return logString.get();
        }

    }
}

