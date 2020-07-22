package com.island.bookingapi.model;

public enum BookingStatus {
    ACTIVE(0),
    CANCEL(1);

    Integer id;

    BookingStatus(final Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    /**
     * Return BookingStatus by id
     *
     * @param id
     * @return BookingStatus
     */
    public BookingStatus getBookingStatusById(Integer id) {
        if (id != null) {
            for (BookingStatus status : BookingStatus.values()) {
                if (status.id.equals(id)) {
                    return status;
                }
            }
        }
        return null;

    }


}
