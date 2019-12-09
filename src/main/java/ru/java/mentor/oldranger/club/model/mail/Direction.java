package ru.java.mentor.oldranger.club.model.mail;

import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.java.mentor.oldranger.club.model.user.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Setter
@NoArgsConstructor
@Entity
@Table(name = "userDirection")
public class Direction {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "periodicity")
    @Enumerated(EnumType.STRING)
    private DirectionType directionType;

    @Column
    private LocalDateTime lastSendTime;

    @OneToOne
    @JoinColumn(name="user_id")
    private User user;

    public Long getId() {
        return id;
    }

    public DirectionType getDirectionType() {
        return directionType;
    }

    public User getUser() {
        return user;
    }

    public LocalDateTime getLastSendTime() {
        return lastSendTime;
    }

    public void setLastSendTime(LocalDateTime lastSendTime) {
        this.lastSendTime = lastSendTime;
    }

    public static DirectionType stringToDirectionType(String direction){
        switch (direction){
            case "ONE_TO_DAY": return DirectionType.ONE_TO_DAY;
            case "TWO_TO_DAY": return DirectionType.TWO_TO_DAY;
            case "ONE_TO_WEEK": return DirectionType.ONE_TO_WEEK;
            default: return DirectionType.NEVER;
        }
    }

}
