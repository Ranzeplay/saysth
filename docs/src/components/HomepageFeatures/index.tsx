import clsx from 'clsx';
import Heading from '@theme/Heading';
import styles from './styles.module.css';
import Translate from '@docusaurus/Translate';

type FeatureItem = {
  title: JSX.Element;
  Svg: React.ComponentType<React.ComponentProps<'svg'>>;
  description: JSX.Element;
};

const FeatureList: FeatureItem[] = [
  {
    title: (
      <Translate
        id="homepage.features.easyToUse.title"
        description="Title of easy to use feature">
        Easy to Use
      </Translate>
    ),
    Svg: require('@site/static/img/undraw_setup_wizard_re_nday.svg').default,
    description: (
      <Translate
        id="homepage.features.easyToUse.description"
        description="Description of easy to use feature">
        Almost out-of-the-box, directly use after setting up necessary configurations.
      </Translate>
    ),
  },
  {
    title: (
      <Translate
        id="homepage.features.multipleModLoaders.title"
        description="Title of multiple mod loaders feature">
        Multiple Mod Loaders
      </Translate>
    ),
    Svg: require('@site/static/img/undraw_abstract_re_l9xy.svg').default,
    description: (
      <Translate
        id="homepage.features.multipleModLoaders.description"
        description="Description of multiple mod loaders feature">
        Support Fabric and NeoForge currently, will add Spigot support in the near future.
      </Translate>
    ),
  },
  {
    title: (
      <Translate
        id="homepage.features.multipleAiPlatforms.title"
        description="Title of multiple AI platforms feature">
        Multiple AI Platforms
      </Translate>
    ),
    Svg: require('@site/static/img/undraw_chat_bot_re_e2gj.svg').default,
    description: (
      <Translate
        id="homepage.features.multipleAiPlatforms.description"
        description="Description of multiple AI platforms feature">
        Support Cloudflare and OpenAI, also OpenAI-compatible platforms that use the same request and response format as OpenAI's.
      </Translate>
    ),
  },
];

function Feature({title, Svg, description}: FeatureItem) {
  return (
    <div className={clsx('col col--4')}>
      <div className="text--center">
        <Svg className={styles.featureSvg} role="img" />
      </div>
      <div className="text--center padding-horiz--md">
        <Heading as="h3">{title}</Heading>
        <p>{description}</p>
      </div>
    </div>
  );
}

export default function HomepageFeatures(): JSX.Element {
  return (
    <section className={styles.features}>
      <div className="container">
        <div className="row">
          {FeatureList.map((props, idx) => (
            <Feature key={idx} {...props} />
          ))}
        </div>
      </div>
    </section>
  );
}
