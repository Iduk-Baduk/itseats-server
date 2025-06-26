package com.idukbaduk.itseats.rider.entity.enums;

import com.idukbaduk.itseats.rider.error.RiderException;
import com.idukbaduk.itseats.rider.error.enums.RiderErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

@Getter
@RequiredArgsConstructor
public enum AssignmentStatus {

    PENDING(null),
    ACCEPTED(PENDING),
    REJECTED(PENDING),
    CANCELED(ACCEPTED);

    private final AssignmentStatus previousStatus;

    public void validateTransitionFrom(AssignmentStatus currentStatus) {
        if (!Objects.equals(this.previousStatus, currentStatus)) {
            throw new RiderException(RiderErrorCode.RIDER_ASSIGNMENT_STATUS_UPDATE_FAIL);
        }
    }
}
