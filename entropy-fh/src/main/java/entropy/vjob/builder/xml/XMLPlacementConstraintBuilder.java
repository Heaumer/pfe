/*
 * Copyright (c) Fabien Hermenier
 *
 *        This file is part of Entropy.
 *
 *        Entropy is free software: you can redistribute it and/or modify
 *        it under the terms of the GNU Lesser General Public License as published by
 *        the Free Software Foundation, either version 3 of the License, or
 *        (at your option) any later version.
 *
 *        Entropy is distributed in the hope that it will be useful,
 *        but WITHOUT ANY WARRANTY; without even the implied warranty of
 *        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *        GNU Lesser General Public License for more details.
 *
 *        You should have received a copy of the GNU Lesser General Public License
 *        along with Entropy.  If not, see <http://www.gnu.org/licenses/>.
 */

package entropy.vjob.builder.xml;

import entropy.vjob.PlacementConstraint;
import entropy.vjob.builder.VJobElementBuilder;

import java.util.List;

/**
 * An interface to specify a generic PlacementConstraint builder.
 *
 * @author Fabien Hermenier
 */
public interface XMLPlacementConstraintBuilder {

    /**
     * Get the identifier of the constraint.
     *
     * @return a string
     */
    String getIdentifier();

    /**
     * Get the signature of the constraint.
     *
     * @return a string
     */
    String getSignature();

    /**
     * Build the constraint
     *
     * @param params the constraint parameters
     * @return the constraint
     * @throws ConstraintBuilderException if an error occurred while building the constraint
     */
    PlacementConstraint buildConstraint(VJobElementBuilder eBuilder, List<Param> params) throws ConstraintBuilderException;
}
