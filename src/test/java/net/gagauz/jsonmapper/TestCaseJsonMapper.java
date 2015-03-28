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

public class TestCaseJsonMapper {

    public static void testSpeed() throws Exception {
        String configString = TestClass.class.getName() + "{method1, parent, children}";
        JsonMapper mapper = JsonMapper.instanse();//(JsonMapperConfig.init(configString));

        TestClass t = new TestClass();
        for (int i = 0; i < 10; i++) {
            TestClass t2 = new TestClass(t);
            for (int i1 = 0; i1 < 10; i1++) {
                TestClass t3 = new TestClass(t2);
                for (int i2 = 0; i2 < 10; i2++) {
                    TestClass t4 = new TestClass(t3);
                }
            }
        }

        String json = mapper.map(t);
        System.out.println(json.toString());
    }

    public void testConfigMapping() throws Exception {
        String configString = TestClass.class.getName() + "{method1 as m1, method2 as m2}";
        JsonMapper mapper = JsonMapper.instanse(JsonMapperConfig.init(configString));

        String json = mapper.map(new TestClass());
        System.out.println(json);
        assert json.replaceAll("[\\t\\s\\n]", "").equals("{m1:'abc',m2:1}");

    }

    public static void main(String[] args) throws Exception {
        //        TestCaseJsonMapper c = new TestCaseJsonMapper();
        //        c.testConfigMapping();
        while (true)
            testSpeed();
    }
}
