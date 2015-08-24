#include <TimerOne.h>//inclui a biblioteca do Timer1
#include <dht11.h>//inclui a biblioteca do DHT11
dht11 DHT; //declara o sensor DHT
#define DHT11_PIN 7 // determina o sensor DHT11 para o pino 7
int cont =0; // inicialização do contador
int GND =14; // somente necessário se não for utilizar protoboard - Acrescenta uma GND
int red =9; // pino correspondente ao led vermelho
int green =10;// pino correspodente ao led verde
int blue =11; // pino correspondente ao led azul
int flagGreen =0; // flag para o led verde
int flagRed  =0; // flag para o led vermelho
int flagBlue =0; //flag para o led azul
void setup()
{
    pinMode(blue,OUTPUT); //declara o pino correspondente a "blue" como saída
    pinMode(green,OUTPUT);//declara o pino correspondente a "green" como saída
    pinMode(red,OUTPUT);//declara o pino correspondente a "red" como saída
    pinMode(GND,OUTPUT);//declara o pino correspondente a "gnd" como saída
    digitalWrite(red,LOW);//altera o nível lógico do pino correspondente ao "red" como baixo
    digitalWrite(GND,LOW);//altera o nível lógico do pino correspondente ao "GND" como baixo
    digitalWrite(blue,LOW);//altera o nível lógico do pino correspondente ao "blue" como baixo
    Serial.begin(9600); //inicializa serial com um baud-rate de 9600
    Timer1.initialize(1000000); //inicializa com 1 segundo
    Timer1.attachInterrupt(Incrementa);//função que será chamada quando houver o estouro do flag de contagem do timer1
    digitalWrite(green,HIGH);//altera o nível lógico do pino correspondente ao "green" como alto.
}

void loop()
{


    if(cont==300) //verifica se o valor de "cont" é igual a 300 (5 min) 
    {
        int chk; // declara a variável "chk" do tipo inteiro
        chk = DHT.read(DHT11_PIN);    // Leitura de dados do sensor
        switch (chk) 
        {
        case DHTLIB_OK: //caso não houver nenhum problema com o sensor ou com os dados vindo do sensor 
            Serial.print("(");//"start byte" do protocolo de envido de dados pela serial
            Serial.print(DHT.temperature,1); //obtém a temperatura pelo sensor
            Serial.print(","); //divisor de dados
            Serial.print(DHT.humidity,1);//obtém a umidade pelo sensor 
            Serial.print(")");//byte de fim de transmissão
            digitalWrite(blue,HIGH);//acende o led azul
            flagBlue =1;//ajusta o flag azul como 1
            delay(2000); // espera 2 segundos com o led azul em alta para indicar o envio dos dados
            digitalWrite(blue,LOW);//desliga o led azul
            flagBlue =0;//ajusta o flag azul como 0
            break;
            
        case DHTLIB_ERROR_CHECKSUM: //caso houver algum erro com o recebimento dos dados através do sensor(Dados errados--> soma checksum incorreta)
            Timer1.detachInterrupt(); // para a contagem do timer1
            Serial.print("(");//"start byte" do protocolo de envido de dados pela serial
            Serial.print("0,ED");//Envia o código de erro referente a este problema
            Serial.print(")");//byte de fim de transmissão
            digitalWrite(green,LOW);// Apaga o led verde de indicação da contagem
            digitalWrite(red,HIGH);// Acende o led vermelho de indicação de erro
            flagRed =1;//ajusta o flag do vermelho como 1 
            break;
        case DHTLIB_ERROR_TIMEOUT://caso houver algum erro com o sensor como não responder(TIMEOUT)
            Timer1.detachInterrupt();// pára a contagem do timer1
            Serial.print("(");//"start byte" do protocolo de envido de dados pela serial
            Serial.print("ED,0");//Envia o código de erro referente a este problema
            Serial.print(")");//byte de fim de transmissão
            digitalWrite(green,LOW);// Apaga o led verde de indicação da contagem
            digitalWrite(red,HIGH);// Acende o led vermelho de indicação de erro
            flagRed=1;//ajusta o flag do vermelho como 1 

            break;
        default:

            break;
        }
        cont =0; //ajusta o valor do contador
        Timer1.setPeriod(1000000);//Programa o timer1  por mais 5 min
    }

}


void Incrementa() //função chamada no estouro do flag do timer1
{
    if(flagGreen==0 && flagRed==0 && flagBlue==0) // se os flags estiverem em zero...parte responsável pela representação do clock de contagem
    {
        digitalWrite(green,LOW);// Apaga o led verde de indicação da contagem
        flagGreen =1;//ajusta o flag do verde como 1
    }
    else if(flagBlue ==1) // se estiver a enviar dados
    {
        digitalWrite(green,LOW);//Apaga o led verde de indicação da contagem
        flagGreen= 1;//ajusta o flag do verde como 1
    }
    else // parte responsável pela representação do clock de contagem
    {
        digitalWrite(green,HIGH);//acende o led verde de indicação da contagem
        flagGreen =0; //ajusta o flag do verde como 0
    }
    cont++; // incrementa o contador a cada um segundo.
}
