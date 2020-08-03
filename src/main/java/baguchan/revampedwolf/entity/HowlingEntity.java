package baguchan.revampedwolf.entity;

public interface HowlingEntity {
    boolean isHowling();

    void setHowling(boolean howling);

    float getHowlAnimationProgress(float delta);
}