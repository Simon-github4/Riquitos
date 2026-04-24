"use client";

import { Phone, MessageCircle, Mail, ArrowRight, Instagram, Facebook } from "lucide-react";

const contactMethods = [
  {
    icon: Phone,
    title: "Teléfono",
    description: "Llamanos de Lunes a Viernes de 9 a 18hs",
    value: "+54 11 5555-1234",
    href: "tel:+541155551234",
    cta: "Llamar ahora",
  },
  {
    icon: MessageCircle,
    title: "WhatsApp",
    description: "Respuesta inmediata todos los días",
    value: "+54 11 5555-1234",
    href: "https://wa.me/5411555512345",
    cta: "Enviar mensaje",
  },
  {
    icon: Mail,
    title: "Email",
    description: "Te respondemos en menos de 24hs",
    value: "ventas@velocitymotors.com",
    href: "mailto:ventas@velocitymotors.com",
    cta: "Enviar email",
  },
];

export function Contact() {
  return (
    <section id="contacto" className="py-24 bg-secondary">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="text-center max-w-2xl mx-auto mb-16">
          <span className="text-primary text-sm font-semibold tracking-widest uppercase">
            Contacto
          </span>
          <h2 className="text-4xl sm:text-5xl font-black text-foreground mt-4 mb-4">
            HABLEMOS
          </h2>
          <p className="text-muted-foreground text-lg">
            Estamos listos para ayudarte a encontrar tu próximo vehículo
          </p>
        </div>

        <div className="grid md:grid-cols-3 gap-5 max-w-4xl mx-auto">
          {contactMethods.map((method, index) => (
            <a
              key={method.title}
              href={method.href}
              target={method.title === "WhatsApp" ? "_blank" : undefined}
              rel={method.title === "WhatsApp" ? "noopener noreferrer" : undefined}
              className="group relative bg-card border border-border rounded-xl p-6 hover:border-primary/50 transition-all duration-300 hover:shadow-xl hover:shadow-primary/5 text-center"
              style={{ animationDelay: `${index * 100}ms` }}
            >
              {/* Icon */}
              <div className="inline-flex items-center justify-center w-12 h-12 rounded-full bg-primary/10 text-primary mb-4 group-hover:bg-primary group-hover:text-primary-foreground transition-all duration-300">
                <method.icon className="w-5 h-5" />
              </div>

              {/* Title */}
              <h3 className="text-lg font-bold text-foreground mb-1">
                {method.title}
              </h3>

              {/* Description */}
              <p className="text-muted-foreground text-xs mb-3">
                {method.description}
              </p>

              {/* Value */}
              <p className="text-foreground text-sm font-semibold mb-4">
                {method.value}
              </p>

              {/* CTA */}
              <span className="inline-flex items-center gap-2 text-primary text-sm font-semibold group-hover:gap-3 transition-all duration-300">
                {method.cta}
                <ArrowRight className="w-4 h-4" />
              </span>
            </a>
          ))}
        </div>

        {/* WhatsApp Button */}
        <div className="text-center mt-12">
          <a
            href="https://wa.me/5411555512345"
            target="_blank"
            rel="noopener noreferrer"
            className="inline-flex items-center gap-3 bg-primary hover:bg-primary/90 text-primary-foreground font-bold px-8 py-4 rounded-lg transition-all duration-300 hover:scale-105 hover:shadow-xl hover:shadow-primary/20"
          >
            <MessageCircle className="w-5 h-5" />
            Contactanos por WhatsApp
          </a>
        </div>
      </div>
    </section>
  );
}
