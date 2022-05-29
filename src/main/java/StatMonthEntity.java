import java.util.Objects;

public class StatMonthEntity {
    private String date;
    private double value;
    private double difference;

    public StatMonthEntity(String date, Double value) {
        this.date = date;
        this.value = value;
        this.difference = 0.0;
    }

    public String getDate() {
        return date;
    }

    public double getValue() {
        return value;
    }

    public double getDifference() {
        return difference;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public void calculateDifference(double difference) {

        if (difference != 0.0) this.difference = this.value - difference;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StatMonthEntity that = (StatMonthEntity) o;
        return Double.compare(that.value, value) == 0 && Double.compare(that.difference, difference) == 0 && date.equals(that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, value, difference);
    }

    @Override
    public String toString() {
        return "StatMonthEntity{" +
                "date='" + date + '\'' +
                ", value=" + value +
                ", difference=" + difference +
                '}';
    }
}
