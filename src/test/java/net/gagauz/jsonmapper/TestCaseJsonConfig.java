/*
 *  Copyright 2013 Michael Gagauz
 *  
 *  This file is part of JsonMapper.
 *
 *  JsonMapper is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  JsonMapper is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *  
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with JsonMapper.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.gagauz.jsonmapper;

import java.io.InputStream;
import java.util.Collection;

public class TestCaseJsonConfig {

    public void testUnexistingMethods() throws Exception {
        String configString = TestClass.class.getName() + "{id,method3}";
        try {
            JsonMapperConfig.init(configString);
            assert false;//Should be unreachable
        } catch (Exception e) {
            assert e.getMessage().contains("id,");
            assert e.getMessage().contains("method3,");
            //ok
        }
    }

    public void testConfigFile() throws Exception {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("sample-config.txt");
        assert is != null;
        JsonMapperConfig config = JsonMapperConfig.init();
        assert config.getMethodsForClass(TestClass.class).size() == 2;
    }

    public void testValidConfig() throws Exception {
        String configString = TestClass.class.getName() + "{method1 as m1, method2}";
        JsonMapperConfig config = JsonMapperConfig.init(configString);
        Collection<MethodAlias> methods = config.getMethodsForClass(TestClass.class);
        assert methods.size() == 2;
        for (MethodAlias ma : methods) {
            if (ma.getMethod().getName().equals("getMethod1")) {
                assert ma.getAliasName().equals("m1");
            }
        }
    }

    public void testDefaultConfig() throws Exception {
        JsonMapperConfig config = JsonMapperConfig.init();
        assert config.getMethodsForClass(TestClass.class).size() == 2;
    }

    public static void main(String[] args) throws Exception {
        TestCaseJsonConfig c = new TestCaseJsonConfig();
        c.testConfigFile();
        c.testDefaultConfig();
        c.testUnexistingMethods();
        c.testValidConfig();
    }
}
