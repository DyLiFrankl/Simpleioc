package com.t.e.util;

import com.t.e.xml.BeanDefinition;
import com.t.e.xml.BeanReference;
import com.t.e.xml.ConstructorArg;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class XmlBeanDefinitionReader {
    public List<BeanDefinition> loadBeanDefinitions(String xmlPath) throws Exception {
        List<BeanDefinition> definitions = new ArrayList<>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new File(xmlPath));

        NodeList beanNodes = doc.getElementsByTagName("bean");
        for (int i = 0; i < beanNodes.getLength(); i++) {
            Node beanNode = beanNodes.item(i);
            if (beanNode.getNodeType() == Node.ELEMENT_NODE) {
                Element beanElement = (Element) beanNode;
                BeanDefinition definition = new BeanDefinition();
                definition.setId(beanElement.getAttribute("id"));
                definition.setClassName(beanElement.getAttribute("class"));
                definition.setScope(beanElement.getAttribute("scope"));

                // 解析构造参数
                NodeList constructorArgs = beanElement.getElementsByTagName("constructor-arg");
                for (int j = 0; j < constructorArgs.getLength(); j++) {
                    Element argElement = (Element) constructorArgs.item(j);
                    ConstructorArg arg = new ConstructorArg();
                    arg.setIndex(Integer.parseInt(argElement.getAttribute("index")));
                    if (argElement.hasAttribute("value")) {
                        arg.setValue(argElement.getAttribute("value"));
                    } else if (argElement.hasAttribute("ref")) {
                        arg.setRef(argElement.getAttribute("ref"));
                    }
                    definition.getConstructorArgs().add(arg);
                }

                // 解析属性
                NodeList properties = beanElement.getElementsByTagName("property");
                for (int j = 0; j < properties.getLength(); j++) {
                    Element propElement = (Element) properties.item(j);
                    String name = propElement.getAttribute("name");
                    if (propElement.hasAttribute("value")) {
                        definition.getProperties().put(name, propElement.getAttribute("value"));
                    } else if (propElement.hasAttribute("ref")) {
                        definition.getProperties().put(name, new BeanReference(propElement.getAttribute("ref")));
                    }
                }
                definitions.add(definition);
            }
        }
        return definitions;
    }
}


