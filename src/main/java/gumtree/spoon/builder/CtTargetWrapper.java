/* *****************************************************************************
 * Copyright (c) 2022, Oracle and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * *****************************************************************************/

package gumtree.spoon.builder;

import spoon.reflect.declaration.CtElement;
import spoon.reflect.path.CtPath;
import spoon.reflect.visitor.CtVisitor;
import spoon.support.reflect.declaration.CtElementImpl;

/**
 * This class wraps any node that appears as an invocation target to provide a means of differentiating
 * between that node and any other child nodes the invocation may have.
 */
public class CtTargetWrapper extends CtElementImpl {

    private CtElement targetElement;

    public CtTargetWrapper(CtElement element, CtElement parent) {
        super();
        this.targetElement = element;
        this.parent = parent;
        setFactory(parent.getFactory());
    }

    @Override
    public void accept(CtVisitor visitor) {
        targetElement.accept(visitor);
    }

    @Override
    public String toString() {
        return (targetElement != null) ? targetElement.toString() : "";
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CtTargetWrapper)) {
            return false;
        }
        if (this == o) {
            return true;
        }
        CtTargetWrapper other = (CtTargetWrapper) o;
        return other.targetElement.equals(targetElement);
    }

    @Override
    public CtPath getPath() {
        return parent.getPath();
    }
}
