#include "BlinkTask.h"

BlinkTask::BlinkTask(int pin)
{
  this->pin = pin;
}

void BlinkTask::init(int period)
{
  Task::init(period);
  led = new Led(pin);
  state = OFF;
}

void BlinkTask::tick()
{
  if (waterState == PRE_ALARM)
  {
  }
  switch (state)
  {
  case OFF:
    led->switchOn();
    state = ON;
    break;
  case ON:
    led->switchOff();
    state = OFF;
    break;
  }
}
else if (waterState == ALARM)
{
  led->switchOn();
}
else if (waterState == NORMAL)
{
  led->switchOff();
}
}