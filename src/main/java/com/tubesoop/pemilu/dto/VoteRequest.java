package com.tubesoop.pemilu.dto;
import jakarta.validation.constraints.NotNull;

public class VoteRequest {

    @NotNull(message = "NIK tidak boleh kosong")
    private String nik;

    @NotNull(message = "ID kandidat tidak boleh kosong")
    private Long candidateId;

    public VoteRequest() {}

    public VoteRequest(String nik, Long candidateId) {
        this.nik = nik;
        this.candidateId = candidateId;
    }

    public String getNik() {
        return nik;
    }

    public void setNik(String nik) {
        this.nik = nik;
    }

    public Long getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(Long candidateId) {
        this.candidateId = candidateId;
    }

    @Override
    public String toString() {
        return "VoteRequest{" +
                "nik='" + nik + '\'' +
                ", candidateId=" + candidateId +
                '}';
    }
}
