'use strict';

var welcomeForm = document.querySelector('#welcomeForm');
var dialogueForm = document.querySelector('#dialogueForm');
welcomeForm.addEventListener('submit', connect, true)
dialogueForm.addEventListener('submit', sendMessage, true)

var stompClient = null;
var name = null;

function connect(event) {
	name = document.querySelector('#name').value.trim();

	if (name) {
		document.querySelector('#welcome-page').classList.add('hidden');
		document.querySelector('#dialogue-page').classList.remove('hidden');

		var socket = new SockJS('http://172.16.0.23:15001/notificationWebsocket');
		stompClient = Stomp.over(socket);
		stompClient.connect({}, connectionSuccess);
	}
	event.preventDefault();
}

function connectionSuccess() {
    stompClient.subscribe('/queue/operational_support_service', onMessageReceived);
	console.log("Successfully subscribed");
}

function sendMessage(event) {

	stompClient.send("/app/notificationProcessed", {}, JSON.stringify({
		id : '466e04f8-bed1-4e89-b2e7-3fc4a69b25de',
		userId: '6f9d6b79-92f0-4204-8401-33d7b685994d',
        result: 'Исполнено'
	}))
	event.preventDefault();
}

function onMessageReceived(payload) {
	//var message = JSON.parse(payload.body);
/*
	stompClient.send("/app/notificationProcessed", {}, JSON.stringify({
		id : '083bd71f-e55f-4f68-95ba-4dcf1a1c48d2',
		userId: '6f9d6b79-92f0-4204-8401-33d7b685994d',
        result: 'Исполнено'
	}))*/
}