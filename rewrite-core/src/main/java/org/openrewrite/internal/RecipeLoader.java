/*
 * Copyright 2025 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.jspecify.annotations.Nullable;
import org.openrewrite.Recipe;

import java.util.HashMap;
import java.util.Map;

public class RecipeLoader {
    @Nullable
    private final ClassLoader classLoader;

    @Getter
    private final ObjectMapper mapper;

    public RecipeLoader(@Nullable ClassLoader classLoader) {
        this.classLoader = classLoader;
        this.mapper = ObjectMappers.propertyBasedMapper(classLoader);
    }

    public Recipe load(String recipeName, @Nullable Map<String, Object> recipeArgs) {
        if (recipeArgs == null || recipeArgs.isEmpty()) {
            try {
                // first try an explicitly-declared zero-arg constructor
                return (Recipe) Class.forName(recipeName, true,
                                classLoader == null ? this.getClass().getClassLoader() : classLoader)
                        .getDeclaredConstructor()
                        .newInstance();
            } catch (ReflectiveOperationException e) {
                return instantiateRecipe(recipeName, new HashMap<>());
            }
        }
        return instantiateRecipe(recipeName, recipeArgs);
    }

    private Recipe instantiateRecipe(String recipeName, Map<String, Object> args) throws IllegalArgumentException {
        Map<Object, Object> withJsonType = new HashMap<>(args);
        withJsonType.put("@c", recipeName);
        return mapper.convertValue(withJsonType, Recipe.class);
    }
}
