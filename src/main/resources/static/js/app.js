// JavaScript mínimo do site. Sem bibliotecas: tudo funciona em qualquer navegador moderno.
// Sem JavaScript o site continua utilizável (o menu aparece aberto e as miniaturas viram links).

(function () {
    'use strict';

    // Menu do topo no celular
    var topo = document.querySelector('.topo');
    var botao = document.querySelector('.menu-botao');
    if (topo && botao) {
        botao.addEventListener('click', function () {
            var aberto = topo.classList.toggle('aberto');
            botao.setAttribute('aria-expanded', aberto ? 'true' : 'false');
        });
    }

    // Galeria do anúncio: clicar numa miniatura troca a foto principal
    var principal = document.getElementById('galeria-principal');
    var legenda = document.getElementById('galeria-legenda');
    var miniaturas = document.querySelectorAll('[data-galeria]');
    if (principal && miniaturas.length) {
        miniaturas.forEach(function (item) {
            item.addEventListener('click', function (evento) {
                evento.preventDefault();
                principal.src = item.getAttribute('href');
                principal.alt = item.getAttribute('data-legenda') || '';
                if (legenda) { legenda.textContent = item.getAttribute('data-legenda') || ''; }
                miniaturas.forEach(function (outra) { outra.classList.remove('ativa'); });
                item.classList.add('ativa');
            });
        });
    }

    // Confirmação antes de ações que não têm volta (formulários com data-confirmar="texto")
    document.querySelectorAll('form[data-confirmar]').forEach(function (form) {
        form.addEventListener('submit', function (evento) {
            if (!window.confirm(form.getAttribute('data-confirmar'))) {
                evento.preventDefault();
            }
        });
    });
})();
