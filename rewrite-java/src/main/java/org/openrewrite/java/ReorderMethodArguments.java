/*
 * Copyright 2020 the original author or authors.
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
package org.openrewrite.java;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeProcessor;
import org.openrewrite.Validated;
import org.openrewrite.internal.lang.NonNull;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JRightPadded;
import org.openrewrite.java.tree.Space;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.openrewrite.Validated.required;

/**
 * This recipe finds method invocations matching the given method pattern and reorders the arguments based on the ordered
 * array of parameter names.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
public class ReorderMethodArguments extends Recipe {

    @NonNull
    private final String methodPattern;

    /**
     * An array of parameter names that indicates the new order in which those arguments should be arranged.
     */
    @NonNull
    private final String[] orderedArgumentNames;

    /**
     * If the original method signature has changed, this is an optional list that indicates the original order
     * in which the arguments were arranged.
     */
    private String[] originalOrderedArgumentNames = new String[0];

    public ReorderMethodArguments(String methodPattern, String[] orderedArgumentNames) {
        this.methodPattern = methodPattern;
        this.orderedArgumentNames = orderedArgumentNames;
    }

    @Override
    protected TreeProcessor<?, ExecutionContext> getProcessor() {
        return new ReorderMethodArgumentsProcessor(new MethodMatcher(methodPattern));
    }

    private class ReorderMethodArgumentsProcessor extends JavaIsoProcessor<ExecutionContext> {
        private final MethodMatcher methodMatcher;

        private ReorderMethodArgumentsProcessor(MethodMatcher methodMatcher) {
            this.methodMatcher = methodMatcher;
        }

        @Override
        public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
            J.MethodInvocation m = super.visitMethodInvocation(method, ctx);

            if (methodMatcher.matches(m) && m.getType() != null) {
                List<String> paramNames = originalOrderedArgumentNames.length == 0 ? m.getType().getParamNames() :
                        Arrays.asList(originalOrderedArgumentNames);

                if (paramNames == null) {
                    throw new IllegalStateException("There is no source attachment for method " + m.getType().getDeclaringType().getFullyQualifiedName() +
                            "." + m.getSimpleName() + "(..). Provide a reference for original parameter names by calling setOriginalParamNames(..)");
                }

                List<JRightPadded<Expression>> originalArgs = m.getArgs().getElem();

                int resolvedParamCount = m.getType().getResolvedSignature() == null ? originalArgs.size() :
                        m.getType().getResolvedSignature().getParamTypes().size();

                int i = 0;
                List<JRightPadded<Expression>> reordered = new ArrayList<>(originalArgs.size());
                List<Space> formattings = new ArrayList<>(originalArgs.size());
                List<Space> rightFormattings = new ArrayList<>(originalArgs.size());

                for (String name : orderedArgumentNames) {
                    int fromPos = paramNames.indexOf(name);
                    if (originalArgs.size() > resolvedParamCount && fromPos == resolvedParamCount - 1) {
                        // this is a varargs argument
                        List<JRightPadded<Expression>> varargs = originalArgs.subList(fromPos, originalArgs.size());
                        reordered.addAll(varargs);
                        for (JRightPadded<Expression> exp : originalArgs.subList(i, (i++) + varargs.size())) {
                            formattings.add(exp.getElem().getPrefix());
                            rightFormattings.add(exp.getAfter());
                        }
                    } else if (fromPos >= 0 && originalArgs.size() > fromPos) {
                        reordered.add(originalArgs.get(fromPos));
                        formattings.add(originalArgs.get(i).getElem().getPrefix());
                        rightFormattings.add(originalArgs.get(i++).getAfter());
                    }
                }

                boolean changed = false;
                i = 0;
                for (JRightPadded<Expression> expression : reordered) {
                    final int index = i;
                    reordered.set(i, expression
                            .map(e -> e.withPrefix(formattings.get(index)))
                            .withAfter(rightFormattings.get(index)));
                    if (reordered.get(i) != originalArgs.get(i)) {
                        changed = true;
                    }
                    i++;
                }

                if (changed) {
                    m = m.withArgs(m.getArgs().withElem(reordered));
                }
            }
            return m;
        }
    }
}
