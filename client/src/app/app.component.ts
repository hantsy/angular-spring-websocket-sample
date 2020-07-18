import { Component, NgZone, OnDestroy, OnInit } from '@angular/core';


@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit, OnDestroy {
  title = 'client';
  message = '';
  messages: any[];
  socket: WebSocket;

  constructor(private zone: NgZone) {
  }

  ngOnInit(): void {
    this.messages = [];
    this.socket = new WebSocket("ws://localhost:8080/ws/messages");
    this.socket.onmessage = event => {
      console.log('onmessage:' + event)
      this.zone.run(() => {
        this.addMessage(event.data);
      })
    }
  }

  addMessage(msg: any) {
    this.messages = [...this.messages, msg];
    //console.log("messages::" + this.messages);
  }

  ngOnDestroy(): void {
    this.socket && this.socket.close();
  }

  sendMessage() {
    console.log("sending message:" + this.message);
    this.socket.send(this.message);
    this.message = null;
  }
}
