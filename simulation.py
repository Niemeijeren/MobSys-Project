from random import random

def main():
    print(pickUpsSavedTimes(1000))

def pickUpsSavedTimes(N):
    total = 0
    if (N > 0):
        for i in range(0,N):
            total += pickUpsSaved(15)

    return total / N


def pickUpsSaved(increase):

    state = "gps"

    gpsToGps = 0.88
    gpsToCompass = 0.12

    compassToGps = 0.875
    compassToCompass = 0.125

    intervalIncrease = increase
    PickUpsSaved = 0
    maxTimeInSeconds = 3600 * 8
    maxTimeCounter = 0

    interval = 5
    while (True):
        if (state == "gps"):
            if (random() > 0.88):
                interval = interval + increase
                state = "compass"
            else:
                #Basically stay on GPS do nothing
                state = "gps"
                interval = 5
        if (state == "compass"):
            if (random() > 0.875):
                interval = interval + increase
                state = "compass"
            else:
                state = "gps"
                interval = 5

        if (interval > 5):
            PickUpsSaved += (interval - 5) / 5

        maxTimeCounter += interval
        if (maxTimeCounter >= maxTimeInSeconds):
            break


    return int(PickUpsSaved)

if __name__ == '__main__':
    main()