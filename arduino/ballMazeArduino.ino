#include <Servo.h>

Servo myservo; 
Servo myservo2; 

// Variavel que armazena o valor lido 
char input[2][5];     

char c;
int sNum=0;
int i=0;

void setup() 
{ 
  // Define que o servo esta ligado a porta 9
  myservo.attach(9);  
  myservo2.attach(10);
 
  Serial.begin(9600);

  // posicao inicial dos motores
  myservo.write(0);
  myservo2.write(0);
  delay(300);
} 

void loop() 
{ 
  if(Serial.available() > 0) {
  
    c = Serial.read();    

    if (c != '\r') {
      
      if(c==';') {
          input[sNum++][i] = '\0';
          i=0;
      } else {
        input[sNum][i++] = c;
      }
    } else {
      
      input[1][i] = '\0';
      i=0;
      sNum=0;

      int valConvertido = map(atoi(input[0]), -10, 10, 0, 90);
      int valConvertido2 = map(atoi(input[1]), -10, 10, 0, 90);

      myservo.write(valConvertido);
      myservo2.write(valConvertido2);
      
      //Serial.print( "X:" );
      //Serial.print( valConvertido );
      //Serial.print( "|Y:" );
      //Serial.println( valConvertido2 );
      delay(10);

    }

  }
                           
}
