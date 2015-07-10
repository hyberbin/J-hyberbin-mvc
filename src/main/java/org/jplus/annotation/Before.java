/*
 * Copyright 2015 www.hyberbin.com.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * Email:hyberbin@qq.com
 */
package org.jplus.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Before说明.
 * @author Hyberbin
 * @date 2013-6-8 15:38:50
 */
@Target({java.lang.annotation.ElementType.METHOD})//该注解只能用在类上
@Retention(RetentionPolicy.RUNTIME)
public @interface Before {
    /**失败后跳转的页面*/
    String send();
    /**失败信息*/
    String message() default "";
}
