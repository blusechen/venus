package com.meidusa.venus.digester;

import org.apache.commons.digester.AbstractObjectCreationFactory;
import org.apache.commons.digester.Digester;
import org.apache.commons.digester.Rule;
import org.xml.sax.Attributes;

/**
 * @author Struct
 */
public class DigesterRuleParser extends org.apache.commons.digester.xmlrules.DigesterRuleParser {
    public DigesterRuleParser() {
    }

    public void addRuleInstances(Digester digester) {
        final String ruleClassName = Rule.class.getName();
        digester.addFactoryCreate("*/bean-property-setter-byAttrname-rule", new BeanPropertySetterRuleFactory());
        digester.addRule("*/bean-property-setter-byAttrname-rule", new PatternRule("pattern"));
        digester.addSetNext("*/bean-property-setter-byAttrname-rule", "add", ruleClassName);

        digester.addFactoryCreate("*/object-create-rule-with-init", new ObjectCreateRuleFactoryWithInit());
        digester.addRule("*/object-create-rule-with-init", new PatternRule("pattern"));
        digester.addSetNext("*/object-create-rule-with-init", "add", ruleClassName);

        super.addRuleInstances(digester);
    }

    /**
     * Factory for creating a ObjectCreateRule
     */
    protected class ObjectCreateRuleFactoryWithInit extends AbstractObjectCreationFactory {
        public Object createObject(Attributes attributes) {
            String className = attributes.getValue("classname");
            String attrName = attributes.getValue("attrname");
            return (attrName == null || attrName.length() == 0) ? new ObjectCreateRuleWithInit(className) : new ObjectCreateRuleWithInit(className, attrName);
        }
    }

    private class PatternRule extends Rule {

        private String attrName;
        private String pattern = null;

        /**
         * @param attrName The name of the attribute containing the pattern
         */
        public PatternRule(String attrName) {
            super();
            this.attrName = attrName;
        }

        /**
         * If a pattern is defined for the attribute, push it onto the pattern stack.
         */
        public void begin(Attributes attributes) {
            pattern = attributes.getValue(attrName);
            if (pattern != null) {
                patternStack.push(pattern);
            }
        }

        /**
         * If there was a pattern for this element, pop it off the pattern stack.
         */
        public void end() {
            if (pattern != null) {
                patternStack.pop();
            }
        }
    }
}
