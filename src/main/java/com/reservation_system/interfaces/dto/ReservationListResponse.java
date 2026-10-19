package com.reservation_system.interfaces.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Response DTO for reservation list queries with pagination metadata.
 */
public class ReservationListResponse {
    
    private List<ReservationResponse> reservations;
    private PaginationMetadata pagination;
    
    public ReservationListResponse() {}
    
    public ReservationListResponse(List<ReservationResponse> reservations, Page<?> page) {
        this.reservations = reservations;
        this.pagination = new PaginationMetadata(
            page.getNumber(),
            page.getTotalPages(),
            page.getTotalElements(),
            page.getSize()
        );
    }
    
    public List<ReservationResponse> getReservations() {
        return reservations;
    }
    
    public void setReservations(List<ReservationResponse> reservations) {
        this.reservations = reservations;
    }
    
    public PaginationMetadata getPagination() {
        return pagination;
    }
    
    public void setPagination(PaginationMetadata pagination) {
        this.pagination = pagination;
    }
    
    public static class PaginationMetadata {
        private int currentPage;
        private int totalPages;
        private long totalElements;
        private int pageSize;
        
        public PaginationMetadata(int currentPage, int totalPages, long totalElements, int pageSize) {
            this.currentPage = currentPage;
            this.totalPages = totalPages;
            this.totalElements = totalElements;
            this.pageSize = pageSize;
        }
        
        public int getCurrentPage() {
            return currentPage;
        }
        
        public void setCurrentPage(int currentPage) {
            this.currentPage = currentPage;
        }
        
        public int getTotalPages() {
            return totalPages;
        }
        
        public void setTotalPages(int totalPages) {
            this.totalPages = totalPages;
        }
        
        public long getTotalElements() {
            return totalElements;
        }
        
        public void setTotalElements(long totalElements) {
            this.totalElements = totalElements;
        }
        
        public int getPageSize() {
            return pageSize;
        }
        
        public void setPageSize(int pageSize) {
            this.pageSize = pageSize;
        }
    }
}
