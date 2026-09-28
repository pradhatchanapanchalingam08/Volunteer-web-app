package com.example.ngo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance_records")
public class AttendanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "signup_id", nullable = false, unique = true)
    private Signup signup;

    @Column(nullable = false)
    private boolean attended;

    @Column(nullable = false)
    private Double hoursContributed = 0.0;

    @Column(nullable = false)
    private LocalDateTime markedDate;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Signup getSignup() { return signup; }
    public void setSignup(Signup signup) { this.signup = signup; }
    public boolean isAttended() { return attended; }
    public void setAttended(boolean attended) { this.attended = attended; }
    public Double getHoursContributed() { return hoursContributed; }
    public void setHoursContributed(Double hoursContributed) { this.hoursContributed = hoursContributed; }
    public LocalDateTime getMarkedDate() { return markedDate; }
    public void setMarkedDate(LocalDateTime markedDate) { this.markedDate = markedDate; }
}
