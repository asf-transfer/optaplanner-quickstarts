/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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
 */

package org.acme.vehiclerouting.domain;

import static org.optaplanner.core.api.score.stream.ConstraintCollectors.sum;

import org.optaplanner.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;

public class VehicleRoutingConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory factory) {
        return new Constraint[] {
                vehicleCapacity(factory),
                distanceFromPreviousStandstill(factory),
                distanceFromLastCustomerToDepot(factory) };
    }

    // ************************************************************************
    // Hard constraints
    // ************************************************************************

    protected Constraint vehicleCapacity(ConstraintFactory factory) {
        return factory.from(Customer.class)
                .groupBy(Customer::getVehicle, sum(Customer::getDemand))
                .filter((vehicle, demand) -> demand > vehicle.getCapacity())
                .penalizeLong(
                        "vehicleCapacity",
                        HardSoftLongScore.ONE_HARD,
                        (vehicle, demand) -> demand - vehicle.getCapacity());
    }

    // ************************************************************************
    // Soft constraints
    // ************************************************************************

    protected Constraint distanceFromPreviousStandstill(ConstraintFactory factory) {
        return factory.from(Customer.class)
                .penalizeLong(
                        "distanceFromPreviousStandstill",
                        HardSoftLongScore.ONE_SOFT,
                        Customer::getDistanceFromPreviousStandstill);
    }

    protected Constraint distanceFromLastCustomerToDepot(ConstraintFactory factory) {
        return factory.from(Customer.class)
                .filter(customer -> customer.getNextCustomer() == null)
                .penalizeLong(
                        "distanceFromLastCustomerToDepot",
                        HardSoftLongScore.ONE_SOFT,
                        customer -> customer.getDistanceToDepot());
    }
}
