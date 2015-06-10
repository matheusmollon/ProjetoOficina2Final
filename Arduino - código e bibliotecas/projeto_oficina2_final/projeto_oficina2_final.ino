#include <TimerOne.h>
#include <dht11.h>

dht11 DHT;
#define DHT11_PIN 7
int cont =0;
int GND =14;
int red =9;
int green =10;
int blue =11;
int flagGreen =0;
int flagRed  =0;
int flagBlue =0;
void setup()
{
    pinMode(blue,OUTPUT);
    pinMode(green,OUTPUT);
    pinMode(red,OUTPUT);
    pinMode(GND,OUTPUT);
    digitalWrite(red,LOW);
    digitalWrite(GND,LOW);
    digitalWrite(blue,LOW);
    Serial.begin(9600); //inicializa serial com um baud-rate de 9600
    Timer1.initialize(1000000); // inicializa com 1 segundo até o estouro
    Timer1.attachInterrupt(Incrementa);
    digitalWrite(green,HIGH);
}

void loop()
{


    if(cont==300)
    {
        int chk;
        chk = DHT.read(DHT11_PIN);    // Leitura de dados do sensor
        switch (chk)
        {
        case DHTLIB_OK:
            Serial.print("(");
            Serial.print(DHT.temperature,1);
            Serial.print(",");
            Serial.print(DHT.humidity,1);
            Serial.print(")");
            digitalWrite(blue,HIGH);
            flagBlue =1;
            delay(2000);
            digitalWrite(blue,LOW);
            flagBlue =0;

            break;
        case DHTLIB_ERROR_CHECKSUM:
            Timer1.detachInterrupt();
            Serial.print("(");
            Serial.print("0,ED");
            Serial.print(")");
            digitalWrite(green,LOW);
            digitalWrite(red,HIGH);
            flagRed =1;
            break;
        case DHTLIB_ERROR_TIMEOUT:
            Timer1.detachInterrupt();
            Serial.print("(");
            Serial.print("ED,0");
            Serial.print(")");
            digitalWrite(green,LOW);
            digitalWrite(red,HIGH);
            flagRed=1;

            break;
        default:

            break;
        }
        cont =0;
        Timer1.setPeriod(1000000);//Programa por 1 segundo
    }

}


void Incrementa()
{
    if(flagGreen==0 && flagRed==0 && flagBlue==0)
    {
        digitalWrite(green,LOW);
        flagGreen =1;
    }
    else if(flagBlue ==1)
    {
        digitalWrite(green,LOW);
        flagGreen= 1;
    }
    else
    {
        digitalWrite(green,HIGH);
        flagGreen =0;
    }
    cont++;
}
